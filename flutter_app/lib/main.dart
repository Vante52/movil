import 'package:cloud_firestore/cloud_firestore.dart';
import 'package:firebase_auth/firebase_auth.dart' as fb_auth;
import 'package:firebase_core/firebase_core.dart';
import 'package:flutter/material.dart';
import 'package:flutter_hooks/flutter_hooks.dart';

import 'firebase_options.dart';
import 'services/chat_service.dart';
import 'services/user_search_service.dart';
import 'widgets/message_bubble.dart';

Future<void> main() async {
  WidgetsFlutterBinding.ensureInitialized();
  await Firebase.initializeApp(
    options: DefaultFirebaseOptions.currentPlatform,
  );
  runApp(const FitMatchChatApp());
}

class FitMatchChatApp extends StatelessWidget {
  const FitMatchChatApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'FitMatch Chat',
      theme: ThemeData(primarySwatch: Colors.teal),
      home: const UserSearchPage(),
    );
  }
}

class UserSearchPage extends HookWidget {
  const UserSearchPage({super.key});

  @override
  Widget build(BuildContext context) {
    final controller = useTextEditingController();
    final query = useState('');
    final currentUserId = fb_auth.FirebaseAuth.instance.currentUser?.uid;
    final searchService = useMemoized(
      () => UserSearchService(FirebaseFirestore.instance),
      const [],
    );

    useEffect(() {
      void listener() => query.value = controller.text.trim();
      controller.addListener(listener);
      return () => controller.removeListener(listener);
    }, const []);

    final searchStream = useMemoized(
      () => searchService.watchUsersByName(query.value),
      [query.value],
    );

    return Scaffold(
      appBar: AppBar(title: const Text('Busca usuarios para chatear')),
      body: GestureDetector(
        onTap: () => FocusScope.of(context).unfocus(),
        child: Column(
          children: [
            Padding(
              padding: const EdgeInsets.fromLTRB(16, 16, 16, 8),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  TextField(
                    controller: controller,
                    decoration: const InputDecoration(
                      labelText: 'Nombre del usuario',
                      prefixIcon: Icon(Icons.search),
                      border: OutlineInputBorder(),
                    ),
                  ),
                  const SizedBox(height: 8),
                  const Text(
                    'Empieza a escribir y verás sugerencias en tiempo real.',
                    style: TextStyle(color: Colors.black54),
                  ),
                ],
              ),
            ),
            Expanded(
              child: StreamBuilder<List<AppUser>>(
                stream: searchStream,
                builder: (context, snapshot) {
                  final users = (snapshot.data ?? [])
                      .where((user) => user.id != currentUserId)
                      .toList();

                  if (snapshot.connectionState == ConnectionState.waiting &&
                      users.isEmpty) {
                    return const Center(child: CircularProgressIndicator());
                  }

                  if (users.isEmpty) {
                    return const Center(
                      child: Text(
                        'Sin resultados todavía. Prueba con un nombre o elige a alguien de la lista reciente.',
                        textAlign: TextAlign.center,
                      ),
                    );
                  }

                  return ListView.builder(
                    padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 8),
                    itemCount: users.length,
                    itemBuilder: (context, index) {
                      final user = users[index];
                      return Card(
                        margin: const EdgeInsets.symmetric(vertical: 6),
                        shape: RoundedRectangleBorder(
                          borderRadius: BorderRadius.circular(12),
                        ),
                        child: ListTile(
                          leading: CircleAvatar(
                            child: Text(user.displayName.isNotEmpty
                                ? user.displayName.characters.first.toUpperCase()
                                : '?'),
                          ),
                          title: Text(user.displayName),
                          subtitle: Text(user.email ?? 'Tap para iniciar conversación'),
                          trailing: const Icon(Icons.chevron_right),
                          onTap: () async {
                            final currentUser = fb_auth.FirebaseAuth.instance.currentUser;
                            if (currentUser == null) {
                              ScaffoldMessenger.of(context).showSnackBar(
                                const SnackBar(
                                  content: Text('Debes iniciar sesión para chatear'),
                                ),
                              );
                              return;
                            }
                            final chatId = await ChatService(FirebaseFirestore.instance)
                                .findOrCreateChat(currentUser.uid, user.id);
                            if (!context.mounted) return;
                            Navigator.of(context).push(
                              MaterialPageRoute(
                                builder: (_) => ChatPage(
                                  chatId: chatId,
                                  otherUser: user,
                                  currentUserId: currentUser.uid,
                                ),
                              ),
                            );
                          },
                        ),
                      );
                    },
                  );
                },
              ),
            ),
          ],
        ),
      ),
    );
  }
}

class ChatPage extends HookWidget {
  const ChatPage({
    required this.chatId,
    required this.otherUser,
    required this.currentUserId,
    super.key,
  });

  final String chatId;
  final AppUser otherUser;
  final String currentUserId;

  @override
  Widget build(BuildContext context) {
    final messageController = useTextEditingController();
    final chatService = useMemoized(
      () => ChatService(FirebaseFirestore.instance),
      const [],
    );

    return Scaffold(
      appBar: AppBar(title: Text(otherUser.displayName)),
      body: Column(
        children: [
          Expanded(
            child: StreamBuilder<List<ChatMessage>>(
              stream: chatService.watchMessages(chatId),
              builder: (context, snapshot) {
                if (!snapshot.hasData) {
                  return const Center(child: CircularProgressIndicator());
                }
                final messages = snapshot.data!;
                return ListView.builder(
                  reverse: true,
                  padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
                  itemCount: messages.length,
                  itemBuilder: (context, index) {
                    final message = messages[index];
                    final isMine = message.senderId == currentUserId;
                    return MessageBubble(
                      message: message.text,
                      isMine: isMine,
                      timestamp: message.sentAt,
                    );
                  },
                );
              },
            ),
          ),
          SafeArea(
            child: Padding(
              padding: const EdgeInsets.all(12),
              child: Row(
                children: [
                  Expanded(
                    child: TextField(
                      controller: messageController,
                      decoration: const InputDecoration(
                        hintText: 'Escribe un mensaje...',
                        border: OutlineInputBorder(),
                      ),
                      minLines: 1,
                      maxLines: 4,
                    ),
                  ),
                  const SizedBox(width: 8),
                  IconButton(
                    icon: const Icon(Icons.send),
                    color: Theme.of(context).colorScheme.primary,
                    onPressed: () async {
                      final text = messageController.text.trim();
                      if (text.isEmpty) return;
                      messageController.clear();
                      await chatService.sendMessage(
                        chatId: chatId,
                        senderId: currentUserId,
                        text: text,
                      );
                    },
                  ),
                ],
              ),
            ),
          ),
        ],
      ),
    );
  }
}
