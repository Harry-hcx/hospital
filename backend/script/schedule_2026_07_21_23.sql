-- Doctor schedules for 2026-07-21, 2026-07-22, 2026-07-23.
--
-- Project note:
-- t_schedule has no schedule_type column. The same schedule records are used by
-- appointment registration and online consultation flows.
--
-- Time plan:
-- 09:00-11:00 outpatient registration friendly slots
-- 14:00-16:00 outpatient registration / daytime consult friendly slots
-- 19:00-20:00 online consultation friendly slot
--
-- This script is idempotent for the three target dates: it clears existing
-- schedule rows on these dates, then regenerates rows for every enabled doctor.

START TRANSACTION;

DELETE FROM t_schedule
WHERE schedule_date IN ('2026-07-21', '2026-07-22', '2026-07-23');

INSERT INTO t_schedule (
  doctor_id,
  hospital_id,
  department_id,
  schedule_date,
  time_slot,
  total_count,
  remain_count,
  status,
  create_time
)
SELECT
  d.id,
  d.hospital_id,
  d.department_id,
  sd.schedule_date,
  ts.time_slot,
  ts.total_count,
  ts.total_count,
  1,
  NOW()
FROM t_doctor d
JOIN (
  SELECT DATE('2026-07-21') AS schedule_date
  UNION ALL SELECT DATE('2026-07-22')
  UNION ALL SELECT DATE('2026-07-23')
) sd
JOIN (
  SELECT '09:00-10:00' AS time_slot, 30 AS total_count, 1 AS sort_no
  UNION ALL SELECT '10:00-11:00', 28, 2
  UNION ALL SELECT '14:00-15:00', 24, 3
  UNION ALL SELECT '15:00-16:00', 22, 4
  UNION ALL SELECT '19:00-20:00', 12, 5
) ts
WHERE d.status = 1
ORDER BY d.id, sd.schedule_date, ts.sort_no;

COMMIT;

-- Verification helpers:
-- SELECT schedule_date, COUNT(*) AS row_count
-- FROM t_schedule
-- WHERE schedule_date IN ('2026-07-21', '2026-07-22', '2026-07-23')
-- GROUP BY schedule_date
-- ORDER BY schedule_date;
--
-- SELECT d.id, d.name, COUNT(s.id) AS schedule_count
-- FROM t_doctor d
-- LEFT JOIN t_schedule s
--   ON s.doctor_id = d.id
--  AND s.schedule_date IN ('2026-07-21', '2026-07-22', '2026-07-23')
-- WHERE d.status = 1
-- GROUP BY d.id, d.name
-- ORDER BY d.id;
