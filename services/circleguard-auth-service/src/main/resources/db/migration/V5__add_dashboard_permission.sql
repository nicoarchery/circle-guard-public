-- Add dashboard viewing permission
INSERT INTO permissions (name, description)
SELECT 'dashboard:view', 'Allow viewing the health analytics dashboard'
WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE name = 'dashboard:view');

-- Map HEALTH_CENTER to the new permission
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p 
WHERE r.name = 'HEALTH_CENTER' AND p.name = 'dashboard:view'
AND NOT EXISTS (
    SELECT 1 FROM role_permissions rp 
    WHERE rp.role_id = r.id AND rp.permission_id = p.id
);
