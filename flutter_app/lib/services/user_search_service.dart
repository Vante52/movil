import 'package:cloud_firestore/cloud_firestore.dart';

import '../models.dart';

class UserSearchService {
  UserSearchService(this._firestore);

  final FirebaseFirestore _firestore;

  Stream<List<AppUser>> watchUsersByName(String query) {
    final normalized = query.trim().toLowerCase();
    final usersRef = _firestore.collection('users').orderBy('nameLowercase');

    // Always stream a small window of users so we can update the dropdown live
    // while typing. When there's a query we filter locally to avoid requiring
    // pre-computed search indexes in Firestore.
    return usersRef.limit(40).snapshots().map((snapshot) {
      final users = snapshot.docs
          .map((doc) => AppUser.fromJson(doc.id, doc.data()))
          .toList();

      if (normalized.isEmpty) {
        return users;
      }

      return users
          .where((user) => user.displayName.toLowerCase().contains(normalized))
          .toList();
    });
  }
}
