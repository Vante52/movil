import 'package:cloud_firestore/cloud_firestore.dart';

import '../models.dart';

class UserSearchService {
  UserSearchService(this._firestore);

  final FirebaseFirestore _firestore;

  Stream<List<AppUser>> watchUsersByName(String query) {
    if (query.isEmpty) {
      return const Stream.empty();
    }
    final normalized = query.toLowerCase();
    final end = '$normalized\uf8ff';

    return _firestore
        .collection('users')
        .orderBy('nameLowercase')
        .startAt([normalized])
        .endAt([end])
        .limit(20)
        .snapshots()
        .map(
          (snapshot) => snapshot.docs
              .map((doc) => AppUser.fromJson(doc.id, doc.data()))
              .toList(),
        );
  }
}
