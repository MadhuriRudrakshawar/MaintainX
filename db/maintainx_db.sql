-- MaintainX seed data
-- 3 users +  network elements +  maintenance windows
USE maintainx_db;

SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE maintenance_window_elements;
TRUNCATE TABLE maintenance_windows;
TRUNCATE TABLE network_element;
TRUNCATE TABLE users;
SET FOREIGN_KEY_CHECKS = 1;

INSERT INTO users (username, role, password) VALUES
  ('admin', 'ADMIN', 'admin123'),
  ('appr', 'APPROVER', 'apr123'),
  ('eng', 'ENGINEER', 'eng123');

INSERT INTO network_element (element_code, name, element_type, region, status) VALUES
  ('NE-001', 'Dublin Core Router A', 'CORE_ROUTER', 'DUBLIN', 'ACTIVE'),
  ('NE-002', 'Dublin Edge Switch A', 'EDGE_SWITCH', 'DUBLIN', 'ACTIVE'),
  ('NE-003', 'Dublin Access Switch A', 'ACCESS_SWITCH', 'DUBLIN', 'ACTIVE'),
  ('NE-004', 'Dublin Aggregation Switch A', 'ACCESS_SWITCH', 'DUBLIN', 'DEACTIVE'),
  ('NE-005', 'Dublin Backbone Firewall A', 'BACKBONE_FIREWALL', 'DUBLIN', 'ACTIVE'),
  ('NE-006', 'Cork Core Router A', 'EDGE_SWITCH', 'CORK', 'DEACTIVE'),
  ('NE-007', 'Cork Edge Switch A', 'EDGE_SWITCH', 'CORK', 'DEACTIVE'),
  ('NE-008', 'Cork Access Switch A', 'ACCESS_SWITCH', 'CORK', 'ACTIVE'),
  ('NE-009', 'Cork Aggregation Switch A', 'ACCESS_SWITCH', 'CORK', 'ACTIVE'),
  ('NE-010', 'Cork Backbone Firewall A', 'AGGREGATION_SWITCH', 'CORK', 'ACTIVE'),
  ('NE-011', 'Galway Core Router A', 'AGGREGATION_SWITCH', 'GALWAY', 'ACTIVE'),
  ('NE-012', 'Galway Edge Switch A', 'EDGE_SWITCH', 'GALWAY', 'DEACTIVE'),
  ('NE-013', 'Galway Access Switch A', 'ACCESS_SWITCH', 'GALWAY', 'DEACTIVE'),
  ('NE-014', 'Galway Aggregation Switch A', 'ACCESS_SWITCH', 'GALWAY', 'ACTIVE'),
  ('NE-015', 'Galway Backbone Firewall A', 'AGGREGATION_SWITCH', 'GALWAY', 'ACTIVE'),
  ('NE-016', 'Donegal Core Router A', 'BACKBONE_FIREWALL', 'DONEGAL', 'ACTIVE'),
  ('NE-017', 'Donegal Edge Switch A', 'EDGE_SWITCH', 'DONEGAL', 'ACTIVE'),
  ('NE-018', 'Donegal Access Switch A', 'ACCESS_SWITCH', 'DONEGAL', 'DEACTIVE'),
  ('NE-019', 'Donegal Aggregation Switch A', 'ACCESS_SWITCH', 'DONEGAL', 'DEACTIVE'),
  ('NE-020', 'Donegal Backbone Firewall A', 'AGGREGATION_SWITCH', 'DONEGAL', 'ACTIVE'),
  ('NE-021', 'National Core Router A', 'CORE_ROUTER', 'NATIONWIDE', 'ACTIVE'),
  ('NE-022', 'National Edge Switch A', 'EDGE_SWITCH', 'NATIONWIDE', 'ACTIVE'),
  ('NE-023', 'National Access Switch A', 'ACCESS_SWITCH', 'NATIONWIDE', 'ACTIVE'),
  ('NE-024', 'National Aggregation Switch A', 'EDGE_SWITCH', 'NATIONWIDE', 'DEACTIVE'),
  ('NE-025', 'National Backbone Firewall A', 'ACCESS_SWITCH', 'NATIONWIDE', 'DEACTIVE');

INSERT INTO maintenance_windows
  (title, description, start_time, end_time, window_status, rejection_reason, decided_by, requested_by)
VALUES
  ('MW-01 Core Patch', 'Core patch batch 01', '2026-03-22 00:30:00', '2026-03-22 02:15:00', 'PENDING', NULL, 'PENDING', (SELECT id FROM users WHERE username = 'eng' LIMIT 1)),
  ('MW-02 Edge Upgrade', 'Edge upgrade batch 02', '2026-03-12 10:00:00', '2026-03-12 12:30:00', 'APPROVED', NULL, 'appr', (SELECT id FROM users WHERE username = 'eng' LIMIT 1)),
  ('MW-03 Access Audit', 'Access audit batch 03', '2026-03-22 03:30:00', '2026-03-22 05:00:00', 'REJECTED', 'Insufficient details provided', 'appr', (SELECT id FROM users WHERE username = 'eng' LIMIT 1)),
  ('MW-04 Agg Tune', 'Aggregation tuning batch 04', '2026-03-22 05:00:00', '2026-03-22 07:30:00', 'PENDING', NULL, 'PENDING', (SELECT id FROM users WHERE username = 'eng' LIMIT 1)),
  ('MW-05 FW Rules', 'Firewall rules cleanup 05', '2026-03-22 06:15:00', '2026-03-22 08:00:00', 'APPROVED', NULL, 'appr', (SELECT id FROM users WHERE username = 'eng' LIMIT 1)),
  ('MW-06 Core Sync', 'Core routing sync 06', '2026-03-23 08:30:00', '2026-03-23 10:45:00', 'PENDING', NULL, 'PENDING', (SELECT id FROM users WHERE username = 'eng' LIMIT 1)),
  ('MW-07 Edge Port', 'Edge port cleanup 07', '2026-03-23 09:00:00', '2026-03-23 11:00:00', 'REJECTED', 'Outside approved maintenance hours', 'appr', (SELECT id FROM users WHERE username = 'eng' LIMIT 1)),
  ('MW-08 HA Test', 'Firewall HA test 08', '2026-03-13 00:00:00', '2026-03-13 23:59:00', 'APPROVED', NULL, 'appr', (SELECT id FROM users WHERE username = 'eng' LIMIT 1)),
  ('MW-09 VLAN Check', 'VLAN consistency 09', '2026-03-23 12:00:00', '2026-03-23 14:15:00', 'PENDING', NULL, 'PENDING', (SELECT id FROM users WHERE username = 'eng' LIMIT 1)),
  ('MW-10 OS Update', 'Switch OS update 10', '2026-03-14 14:30:00', '2026-03-14 16:30:00', 'APPROVED', NULL, 'appr', (SELECT id FROM users WHERE username = 'eng' LIMIT 1)),
  ('MW-11 Kernel Patch', 'Kernel patch 11', '2026-03-24 15:00:00', '2026-03-24 17:45:00', 'PENDING', NULL, 'PENDING', (SELECT id FROM users WHERE username = 'eng' LIMIT 1)),
  ('MW-12 Security Hardening', 'Security hardening 12', '2026-03-24 17:00:00', '2026-03-24 19:00:00', 'REJECTED', 'Risk too high / missing rollback plan', 'appr', (SELECT id FROM users WHERE username = 'eng' LIMIT 1)),
  ('MW-13 Cert Rotate', 'Certificate rotation 13', '2026-03-24 18:30:00', '2026-03-24 20:30:00', 'APPROVED', NULL, 'appr', (SELECT id FROM users WHERE username = 'eng' LIMIT 1)),
  ('MW-14 PoE Tune', 'PoE tuning 14', '2026-03-24 20:00:00', '2026-03-24 22:00:00', 'PENDING', NULL, 'PENDING', (SELECT id FROM users WHERE username = 'eng' LIMIT 1)),
  ('MW-15 Telemetry', 'Telemetry refresh 15', '2026-03-25 21:30:00', '2026-03-25 23:30:00', 'APPROVED', NULL, 'appr', (SELECT id FROM users WHERE username = 'eng' LIMIT 1)),
  ('MW-16 Interface Audit', 'Interface audit 16', '2026-03-25 00:00:00', '2026-03-25 01:30:00', 'PENDING', NULL, 'PENDING', (SELECT id FROM users WHERE username = 'eng' LIMIT 1)),
  ('MW-17 QoS Update', 'QoS profile update 17', '2026-03-25 04:15:00', '2026-03-25 06:00:00', 'APPROVED', NULL, 'appr', (SELECT id FROM users WHERE username = 'eng' LIMIT 1)),
  ('MW-18 Policy Review', 'Policy review 18', '2026-03-26 07:45:00', '2026-03-26 09:15:00', 'REJECTED', 'Not aligned with change calendar', 'appr', (SELECT id FROM users WHERE username = 'eng' LIMIT 1)),
  ('MW-19 STP Review', 'STP review 19', '2026-03-26 10:30:00', '2026-03-26 12:00:00', 'APPROVED', NULL, 'appr', (SELECT id FROM users WHERE username = 'eng' LIMIT 1)),
  ('MW-20 Redundancy Test', 'Redundancy validation 20', '2026-03-26 13:15:00', '2026-03-26 15:15:00', 'PENDING', NULL, 'PENDING', (SELECT id FROM users WHERE username = 'eng' LIMIT 1)),
  ('MW-21 Core Patch', 'Core patch batch 21', '2026-03-27 16:45:00', '2026-03-27 18:15:00', 'APPROVED', NULL, 'appr', (SELECT id FROM users WHERE username = 'eng' LIMIT 1)),
  ('MW-22 Config Cleanup', 'Config cleanup 22', '2026-03-27 19:30:00', '2026-03-27 21:00:00', 'PENDING', NULL, 'PENDING', (SELECT id FROM users WHERE username = 'eng' LIMIT 1)),
  ('MW-23 DR Drill', 'DR drill 23', '2026-03-27 22:15:00', '2026-03-27 23:45:00', 'APPROVED', NULL, 'appr', (SELECT id FROM users WHERE username = 'eng' LIMIT 1)),
  ('MW-24 Firmware Minor', 'Firmware update 24', '2026-03-28 01:45:00', '2026-03-28 03:15:00', 'REJECTED', 'Insufficient details provided', 'appr', (SELECT id FROM users WHERE username = 'eng' LIMIT 1)),
  ('MW-25 Capacity Expand', 'Capacity expansion 25', '2026-03-28 23:00:00', '2026-03-28 23:50:00', 'PENDING', NULL, 'PENDING', (SELECT id FROM users WHERE username = 'eng' LIMIT 1));

INSERT INTO maintenance_window_elements (maintenance_window_id, network_element_id)
SELECT mw.id, ne.id
FROM (
  SELECT 'MW-01 Core Patch' AS mw_title, 'NE-001' AS ne_code UNION ALL
  SELECT 'MW-01 Core Patch', 'NE-002' UNION ALL
  SELECT 'MW-02 Edge Upgrade', 'NE-003' UNION ALL
  SELECT 'MW-02 Edge Upgrade', 'NE-005' UNION ALL
  SELECT 'MW-03 Access Audit', 'NE-005' UNION ALL
  SELECT 'MW-03 Access Audit', 'NE-008' UNION ALL
  SELECT 'MW-04 Agg Tune', 'NE-010' UNION ALL
  SELECT 'MW-04 Agg Tune', 'NE-008' UNION ALL
  SELECT 'MW-05 FW Rules', 'NE-009' UNION ALL
  SELECT 'MW-05 FW Rules', 'NE-010' UNION ALL
  SELECT 'MW-06 Core Sync', 'NE-011' UNION ALL
  SELECT 'MW-06 Core Sync', 'NE-014' UNION ALL
  SELECT 'MW-07 Edge Port', 'NE-011' UNION ALL
  SELECT 'MW-07 Edge Port', 'NE-014' UNION ALL
  SELECT 'MW-08 HA Test', 'NE-015' UNION ALL
  SELECT 'MW-08 HA Test', 'NE-016' UNION ALL
  SELECT 'MW-09 VLAN Check', 'NE-017' UNION ALL
  SELECT 'MW-09 VLAN Check', 'NE-016' UNION ALL
  SELECT 'MW-10 OS Update', 'NE-017' UNION ALL
  SELECT 'MW-10 OS Update', 'NE-020' UNION ALL
  SELECT 'MW-11 Kernel Patch', 'NE-021' UNION ALL
  SELECT 'MW-11 Kernel Patch', 'NE-022' UNION ALL
  SELECT 'MW-12 Security Hardening', 'NE-023' UNION ALL
  SELECT 'MW-12 Security Hardening', 'NE-021' UNION ALL
  SELECT 'MW-13 Cert Rotate', 'NE-023' UNION ALL
  SELECT 'MW-13 Cert Rotate', 'NE-001' UNION ALL
  SELECT 'MW-14 PoE Tune', 'NE-002' UNION ALL
  SELECT 'MW-14 PoE Tune', 'NE-003' UNION ALL
  SELECT 'MW-15 Telemetry', 'NE-001' UNION ALL
  SELECT 'MW-15 Telemetry', 'NE-005' UNION ALL
  SELECT 'MW-16 Interface Audit', 'NE-009' UNION ALL
  SELECT 'MW-16 Interface Audit', 'NE-010' UNION ALL
  SELECT 'MW-17 QoS Update', 'NE-008' UNION ALL
  SELECT 'MW-17 QoS Update', 'NE-009' UNION ALL
  SELECT 'MW-18 Policy Review', 'NE-010' UNION ALL
  SELECT 'MW-18 Policy Review', 'NE-011' UNION ALL
  SELECT 'MW-19 STP Review', 'NE-014' UNION ALL
  SELECT 'MW-19 STP Review', 'NE-015' UNION ALL
  SELECT 'MW-20 Redundancy Test', 'NE-014' UNION ALL
  SELECT 'MW-20 Redundancy Test', 'NE-015' UNION ALL
  SELECT 'MW-21 Core Patch', 'NE-016' UNION ALL
  SELECT 'MW-21 Core Patch', 'NE-017' UNION ALL
  SELECT 'MW-22 Config Cleanup', 'NE-017' UNION ALL
  SELECT 'MW-22 Config Cleanup', 'NE-020' UNION ALL
  SELECT 'MW-23 DR Drill', 'NE-020' UNION ALL
  SELECT 'MW-23 DR Drill', 'NE-021' UNION ALL
  SELECT 'MW-24 Firmware Minor', 'NE-022' UNION ALL
  SELECT 'MW-24 Firmware Minor', 'NE-023' UNION ALL
  SELECT 'MW-25 Capacity Expand', 'NE-022' UNION ALL
  SELECT 'MW-25 Capacity Expand', 'NE-021'
) map_rows
JOIN maintenance_windows mw ON mw.title = map_rows.mw_title
JOIN network_element ne ON ne.element_code = map_rows.ne_code;

-- Optional quick checks:
-- SELECT COUNT(*) AS users_count FROM users; -- 3
-- SELECT COUNT(*) AS ne_count FROM network_element; -- 25
-- SELECT COUNT(*) AS mw_count FROM maintenance_windows; -- 25
-- SELECT COUNT(*) AS mwe_count FROM maintenance_window_elements; -- 50
