import 'package:cloud_firestore/cloud_firestore.dart';

import '../models.dart';

class UserSearchService {
  UserSearchService(this._firestore);

  final FirebaseFirestore _firestore;

  Stream<List<AppUser>> watchUsersByName(String query) {
    final normalized = query.trim().toLowerCase();

    // Stream the full users collection so we don't rely on Firestore indexes
    // that might not exist (e.g., orderBy on computed lowercase fields). The
    // client filters by the search term and keeps the list stable by sorting
    // alphabetically.
    return _firestore.collection('users').snapshots().map((snapshot) {
      final users = snapshot.docs
          .map((doc) => AppUser.fromJson(doc.id, doc.data()))
          .toList()
        ..sort((a, b) => a.displayName.compareTo(b.displayName));

      if (normalized.isEmpty) {
        return users;
      }

      return users
          .where((user) => user.displayName.toLowerCase().contains(normalized))
          .toList();
    });
  }
}
