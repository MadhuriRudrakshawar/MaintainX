SET FOREIGN_KEY_CHECKS = 0;

DELETE FROM maintenance_window_elements;
DELETE FROM maintenance_windows;
DELETE FROM audit_log;
DELETE FROM network_element;
DELETE FROM users;

ALTER TABLE maintenance_window_elements AUTO_INCREMENT = 1;
ALTER TABLE maintenance_windows AUTO_INCREMENT = 1;
ALTER TABLE audit_log AUTO_INCREMENT = 1;
ALTER TABLE network_element AUTO_INCREMENT = 1;
ALTER TABLE users AUTO_INCREMENT = 1;

SET FOREIGN_KEY_CHECKS = 1;

INSERT INTO users (id, username, role, password) VALUES
    (1, 'admin@mail.com', 'ADMIN', '$2a$10$5CCzNxf59nYNlanROJwwx.pVBa5SDMKa3Ys2pxrEeV5bBYzcoUPz.'),
    (2, 'appr@mail.com', 'APPROVER', '$2a$10$uszOkC03SB.46LEcjyOdIePvv9KWyY3Akvj3AKBfbbQV31IpIYWQi'),
    (3, 'eng@mail.com', 'ENGINEER', '$2a$10$9leo89CMVLqLWrMinO8t8.ewXKdeSns6h0nvG2ktWkdwjREo1KYWS');

INSERT INTO network_element (id, element_code, name, element_type, region, status) VALUES
    (1, 'NE-001', 'Dublin Core Router 01', 'CORE_ROUTER', 'DUBLIN', 'ACTIVE'),
    (2, 'NE-002', 'Dublin Core Router 02', 'CORE_ROUTER', 'DUBLIN', 'ACTIVE'),
    (3, 'NE-003', 'Cork Edge Switch 01', 'EDGE_SWITCH', 'CORK', 'ACTIVE'),
    (4, 'NE-004', 'Cork Edge Switch 02', 'EDGE_SWITCH', 'CORK', 'ACTIVE'),
    (5, 'NE-005', 'Galway Access Switch 01', 'ACCESS_SWITCH', 'GALWAY', 'ACTIVE'),
    (6, 'NE-006', 'Galway Access Switch 02', 'ACCESS_SWITCH', 'GALWAY', 'ACTIVE'),
    (7, 'NE-007', 'Donegal Aggregation 01', 'AGGREGATION_SWITCH', 'DONEGAL', 'ACTIVE'),
    (8, 'NE-008', 'Donegal Aggregation 02', 'AGGREGATION_SWITCH', 'DONEGAL', 'ACTIVE'),
    (9, 'NE-009', 'Nationwide Backbone Firewall 01', 'BACKBONE_FIREWALL', 'NATIONWIDE', 'ACTIVE'),
    (10, 'NE-010', 'Nationwide Backbone Firewall 02', 'BACKBONE_FIREWALL', 'NATIONWIDE', 'ACTIVE'),
    (11, 'NE-011', 'Dublin Access Switch 01', 'ACCESS_SWITCH', 'DUBLIN', 'ACTIVE'),
    (12, 'NE-012', 'Dublin Access Switch 02', 'ACCESS_SWITCH', 'DUBLIN', 'ACTIVE'),
    (13, 'NE-013', 'Cork Aggregation 01', 'AGGREGATION_SWITCH', 'CORK', 'ACTIVE'),
    (14, 'NE-014', 'Cork Aggregation 02', 'AGGREGATION_SWITCH', 'CORK', 'ACTIVE'),
    (15, 'NE-015', 'Galway Core Router 01', 'CORE_ROUTER', 'GALWAY', 'ACTIVE'),
    (16, 'NE-016', 'Donegal Edge Switch 01', 'EDGE_SWITCH', 'DONEGAL', 'ACTIVE'),
    (17, 'NE-017', 'Nationwide Access Switch 01', 'ACCESS_SWITCH', 'NATIONWIDE', 'ACTIVE'),
    (18, 'NE-018', 'Dublin Core Router 03', 'CORE_ROUTER', 'DUBLIN', 'DEACTIVE'),
    (19, 'NE-019', 'Cork Edge Switch 03', 'EDGE_SWITCH', 'CORK', 'DEACTIVE'),
    (20, 'NE-020', 'Galway Access Switch 03', 'ACCESS_SWITCH', 'GALWAY', 'DEACTIVE'),
    (21, 'NE-021', 'Donegal Aggregation 03', 'AGGREGATION_SWITCH', 'DONEGAL', 'DEACTIVE'),
    (22, 'NE-022', 'Nationwide Firewall 03', 'BACKBONE_FIREWALL', 'NATIONWIDE', 'DEACTIVE'),
    (23, 'NE-023', 'Dublin Access Switch 03', 'ACCESS_SWITCH', 'DUBLIN', 'DEACTIVE'),
    (24, 'NE-024', 'Cork Core Router 03', 'CORE_ROUTER', 'CORK', 'DEACTIVE'),
    (25, 'NE-025', 'Galway Edge Switch 03', 'EDGE_SWITCH', 'GALWAY', 'DEACTIVE');

INSERT INTO maintenance_windows (
    id,
    title,
    description,
    start_time,
    end_time,
    window_status,
    rejection_reason,
    decided_by,
    execution_status,
    requested_by
) VALUES
    (1, 'Dublin core redundancy test', 'Pre-dawn routing redundancy verification.', '2026-03-18 06:00:00', '2026-03-18 08:00:00', 'APPROVED', NULL, 'appr@mail.com', 'PLANNED', 3),
    (2, 'Cork edge software patch', 'Non-disruptive edge switch patching.', '2026-03-18 11:30:00', '2026-03-18 13:00:00', 'PENDING', NULL, 'PENDING', 'PLANNED', 3),
    (3, 'Galway access VLAN cleanup', 'Rejected due to incomplete rollback notes.', '2026-03-18 14:00:00', '2026-03-18 16:00:00', 'REJECTED', 'Insufficient details provided', 'appr@mail.com', 'PLANNED', 3),
    (4, 'Galway access optics check', 'Morning optics validation for access switch.', '2026-03-19 06:00:00', '2026-03-19 08:00:00', 'APPROVED', NULL, 'appr@mail.com', 'PLANNED', 3),
    (5, 'Donegal aggregation sync', 'Aggregation pair sync and health review.', '2026-03-19 11:30:00', '2026-03-19 13:00:00', 'APPROVED', NULL, 'appr@mail.com', 'PLANNED', 3),
    (6, 'Nationwide firewall rule review', 'Pending review of firewall policy tidy-up.', '2026-03-19 16:30:00', '2026-03-19 18:00:00', 'PENDING', NULL, 'PENDING', 'PLANNED', 3),
    (7, 'Backbone firewall HA validation', 'Validate failover on backbone firewall pair.', '2026-03-20 06:00:00', '2026-03-20 08:00:00', 'APPROVED', NULL, 'appr@mail.com', 'PLANNED', 3),
    (8, 'Dublin access firmware rollout', 'Rejected because rollback plan was incomplete.', '2026-03-20 11:30:00', '2026-03-20 12:30:00', 'REJECTED', 'Risk too high / missing rollback plan', 'appr@mail.com', 'PLANNED', 3),
    (9, 'Cork aggregation maintenance', 'Pending aggregation uplink validation.', '2026-03-20 14:00:00', '2026-03-20 16:00:00', 'PENDING', NULL, 'PENDING', 'PLANNED', 3),
    (10, 'Galway core memory tuning', 'Apply approved memory tuning change.', '2026-03-21 06:00:00', '2026-03-21 07:30:00', 'APPROVED', NULL, 'appr@mail.com', 'PLANNED', 3),
    (11, 'Donegal edge capacity review', 'Capacity verification on remote edge devices.', '2026-03-21 11:30:00', '2026-03-21 13:00:00', 'APPROVED', NULL, 'appr@mail.com', 'PLANNED', 3),
    (12, 'Dublin core route policy update', 'Pending route policy refresh.', '2026-03-21 14:00:00', '2026-03-21 15:30:00', 'PENDING', NULL, 'PENDING', 'PLANNED', 3),
    (13, 'Dublin and Cork resilience drill', 'Joint resilience drill across two elements.', '2026-03-22 06:00:00', '2026-03-22 08:00:00', 'APPROVED', NULL, 'appr@mail.com', 'PLANNED', 3),
    (14, 'Cork edge port cleanup', 'Pending cleanup of unused edge ports.', '2026-03-22 11:30:00', '2026-03-22 13:00:00', 'PENDING', NULL, 'PENDING', 'PLANNED', 3),
    (15, 'Galway access routing prep', 'Rejected because request was outside change calendar alignment.', '2026-03-22 16:30:00', '2026-03-22 18:00:00', 'REJECTED', 'Not aligned with change calendar', 'appr@mail.com', 'PLANNED', 3),
    (16, 'Donegal aggregation failover drill', 'Approved failover drill for aggregation switch.', '2026-03-23 06:00:00', '2026-03-23 08:00:00', 'APPROVED', NULL, 'appr@mail.com', 'PLANNED', 3),
    (17, 'Firewall and aggregation inspection', 'Joint inspection window for backbone and aggregation.', '2026-03-23 11:30:00', '2026-03-23 13:00:00', 'APPROVED', NULL, 'appr@mail.com', 'PLANNED', 3),
    (18, 'Firewall policy tidy-up', 'Pending cleanup of stale policy objects.', '2026-03-23 14:00:00', '2026-03-23 15:30:00', 'PENDING', NULL, 'PENDING', 'PLANNED', 3),
    (19, 'Access switch uplink check', 'Approved uplink verification across Dublin access layer.', '2026-03-24 06:00:00', '2026-03-24 08:00:00', 'APPROVED', NULL, 'appr@mail.com', 'PLANNED', 3),
    (20, 'Cork aggregation housekeeping', 'Rejected due to insufficient details on execution sequence.', '2026-03-24 11:30:00', '2026-03-24 13:00:00', 'REJECTED', 'Insufficient details provided', 'appr@mail.com', 'PLANNED', 3),
    (21, 'Cork and Galway service prep', 'Pending service preparation on aggregation and core layers.', '2026-03-24 14:00:00', '2026-03-24 16:00:00', 'PENDING', NULL, 'PENDING', 'PLANNED', 3),
    (22, 'Donegal edge baseline capture', 'Approved capture of pre-change baseline.', '2026-03-25 06:00:00', '2026-03-25 08:00:00', 'APPROVED', NULL, 'appr@mail.com', 'PLANNED', 3),
    (23, 'Nationwide access and Dublin core tune', 'Approved dual-element tuning window.', '2026-03-25 11:30:00', '2026-03-25 13:00:00', 'APPROVED', NULL, 'appr@mail.com', 'PLANNED', 3),
    (24, 'Dublin core fan tray review', 'Pending fan tray health review.', '2026-03-25 16:30:00', '2026-03-25 18:00:00', 'PENDING', NULL, 'PENDING', 'PLANNED', 3),
    (25, 'Cork edge stability test', 'Approved stability testing after software cleanup.', '2026-03-26 06:00:00', '2026-03-26 08:00:00', 'APPROVED', NULL, 'appr@mail.com', 'PLANNED', 3),
    (26, 'Galway access access-list review', 'Rejected because the rollback plan was missing.', '2026-03-26 11:30:00', '2026-03-26 13:00:00', 'REJECTED', 'Risk too high / missing rollback plan', 'appr@mail.com', 'PLANNED', 3),
    (27, 'Galway and Donegal patch prep', 'Pending pre-checks for coordinated patching.', '2026-03-26 14:00:00', '2026-03-26 16:00:00', 'PENDING', NULL, 'PENDING', 'PLANNED', 3),
    (28, 'Donegal aggregation optics refresh', 'Approved optics refresh on aggregation uplinks.', '2026-03-27 06:00:00', '2026-03-27 08:00:00', 'APPROVED', NULL, 'appr@mail.com', 'PLANNED', 3),
    (29, 'Firewall peer review', 'Approved peer review for firewall and backbone elements.', '2026-03-27 11:30:00', '2026-03-27 13:00:00', 'APPROVED', NULL, 'appr@mail.com', 'PLANNED', 3),
    (30, 'Multi-site pre-weekend checks', 'Pending pre-weekend checks across selected active elements.', '2026-03-28 14:00:00', '2026-03-28 16:00:00', 'PENDING', NULL, 'PENDING', 'PLANNED', 3);

INSERT INTO maintenance_window_elements (maintenance_window_id, network_element_id) VALUES
    (1, 1), (1, 2),
    (2, 3),
    (3, 4), (3, 5),
    (4, 6),
    (5, 7), (5, 8),
    (6, 9),
    (7, 10), (7, 11),
    (8, 12),
    (9, 13), (9, 14),
    (10, 15),
    (11, 16), (11, 17),
    (12, 1),
    (13, 2), (13, 3),
    (14, 4),
    (15, 5), (15, 6),
    (16, 7),
    (17, 8), (17, 9),
    (18, 10),
    (19, 11), (19, 12),
    (20, 13),
    (21, 14), (21, 15),
    (22, 16),
    (23, 17), (23, 1),
    (24, 2),
    (25, 3), (25, 4),
    (26, 5),
    (27, 6), (27, 7),
    (28, 8),
    (29, 9), (29, 10),
    (30, 11), (30, 13), (30, 16);
