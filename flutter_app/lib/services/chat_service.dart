import 'package:cloud_firestore/cloud_firestore.dart';

import '../models.dart';

class ChatService {
  ChatService(this._firestore);

  final FirebaseFirestore _firestore;

  Future<String> findOrCreateChat(String userId, String otherUserId) async {
    final existing = await _firestore
        .collection('chats')
        .where('members', arrayContains: userId)
        .get();

    for (final doc in existing.docs) {
      final members = List<String>.from(doc['members'] as List<dynamic>);
      if (members.contains(otherUserId)) {
        return doc.id;
      }
    }

    final created = await _firestore.collection('chats').add({
      'members': [userId, otherUserId],
      'createdAt': FieldValue.serverTimestamp(),
    });
    return created.id;
  }

  Stream<List<ChatMessage>> watchMessages(String chatId) {
    return _firestore
        .collection('chats')
        .doc(chatId)
        .collection('messages')
        .orderBy('sentAt', descending: true)
        .snapshots()
        .map(
          (snapshot) => snapshot.docs
              .map((doc) => ChatMessage.fromJson(doc.data()))
              .toList(),
        );
  }

  Future<void> sendMessage({
    required String chatId,
    required String senderId,
    required String text,
  }) async {
    await _firestore.collection('chats').doc(chatId).collection('messages').add({
      'senderId': senderId,
      'text': text,
      'sentAt': FieldValue.serverTimestamp(),
    });
  }
}
