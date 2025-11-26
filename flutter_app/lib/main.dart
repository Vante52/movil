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

    useEffect(() {
      controller.addListener(() => query.value = controller.text.trim());
      return null;
    }, const []);

    final searchStream = useMemoized(
      () => UserSearchService(FirebaseFirestore.instance)
          .watchUsersByName(query.value),
      [query.value],
    );

    return Scaffold(
      appBar: AppBar(title: const Text('Busca usuarios para chatear')),
      body: Column(
        children: [
          Padding(
            padding: const EdgeInsets.all(16),
            child: TextField(
              controller: controller,
              decoration: const InputDecoration(
                labelText: 'Nombre del usuario',
                prefixIcon: Icon(Icons.search),
              ),
            ),
          ),
          Expanded(
            child: StreamBuilder<List<AppUser>>(
              stream: searchStream,
              builder: (context, snapshot) {
                if (snapshot.connectionState == ConnectionState.waiting) {
                  return const Center(child: CircularProgressIndicator());
                }
                final users = snapshot.data ?? [];
                if (users.isEmpty) {
                  return const Center(child: Text('No hay usuarios con ese nombre'));
                }
                return ListView.separated(
                  itemCount: users.length,
                  separatorBuilder: (_, __) => const Divider(height: 1),
                  itemBuilder: (context, index) {
                    final user = users[index];
                    return ListTile(
                      title: Text(user.displayName),
                      subtitle: Text(user.email ?? ''),
                      onTap: () async {
                        final currentUser = fb_auth.FirebaseAuth.instance.currentUser;
                        if (currentUser == null) {
                          ScaffoldMessenger.of(context).showSnackBar(
                            const SnackBar(content: Text('Debes iniciar sesión para chatear')),
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
                    );
                  },
                );
              },
            ),
          ),
        ],
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
