import 'package:cloud_firestore/cloud_firestore.dart';

class AppUser {
  AppUser({
    required this.id,
    required this.displayName,
    this.email,
  });

  factory AppUser.fromJson(String id, Map<String, dynamic> json) {
    return AppUser(
      id: id,
      displayName: json['name'] as String? ?? 'Sin nombre',
      email: json['email'] as String?,
    );
  }

  final String id;
  final String displayName;
  final String? email;
}

class ChatMessage {
  ChatMessage({
    required this.senderId,
    required this.text,
    required this.sentAt,
  });

  factory ChatMessage.fromJson(Map<String, dynamic> json) {
    return ChatMessage(
      senderId: json['senderId'] as String? ?? '',
      text: json['text'] as String? ?? '',
      sentAt: (json['sentAt'] as Timestamp?)?.toDate() ?? DateTime.now(),
    );
  }

  final String senderId;
  final String text;
  final DateTime sentAt;
}
