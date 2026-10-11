ALTER TABLE user_notifications
    DROP CONSTRAINT chk_user_notification_type;

ALTER TABLE user_notifications
    ADD CONSTRAINT chk_user_notification_type CHECK (
        type IN (
            'CONTINUE_LESSON',
            'STREAK_REMINDER',
            'NEW_LESSON',
            'LESSON_UPDATED',
            'PLUS_UPGRADE'
        )
    );
