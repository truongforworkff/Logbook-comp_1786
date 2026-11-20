import 'package:eventa_user/features/agenda/data/catalog_repository.dart';
import 'package:eventa_user/features/agenda/data/event_info.dart';

SessionInfo timedSession(String id, String start, String end) => SessionInfo(
  id: id,
  name: 'Session $id',
  time: start,
  endTime: end,
  speaker: '',
  room: 'A201',
  type: 'Workshop',
  description: '',
);

EventInfo timedEvent(String id, String date, List<SessionInfo> sessions) =>
    EventInfo(
      id: id,
      name: 'Sự kiện $id',
      date: DateTime.parse(date),
      location: 'Hà Nội',
      organiser: 'Eventa',
      type: 'Workshop',
      status: 'Upcoming',
      categoryCode: 'AI',
      image: '',
      description: '',
      source: '',
      sessions: sessions,
    );

class ConflictCatalog extends CatalogRepository {
  final entries = [
    timedEvent('one', '2026-12-01', [timedSession('a', '09:00', '10:00')]),
    timedEvent('two', '2026-12-01', [timedSession('b', '09:30', '10:30')]),
  ];
  @override
  Stream<CatalogData> watch() => Stream.value(CatalogData(entries));
}
