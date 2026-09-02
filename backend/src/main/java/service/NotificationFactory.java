package service;

/**
 * Factory class for creating NotificationService instances based on channel type.
 * Design Pattern: Factory Method Pattern
 */
public class NotificationFactory {

    public enum Channel {
        EMAIL,
        SMS
    }

    /**
     * Creates and returns the concrete NotificationService implementation.
     *
     * @param channel Desired notification channel (EMAIL or SMS)
     * @return Concrete NotificationService instance
     */
    public static NotificationService getNotificationService(Channel channel) {
        if (channel == null) {
            return new SmsNotificationService(); // Default channel
        }
        return switch (channel) {
            case EMAIL -> new EmailNotificationService();
            case SMS -> new SmsNotificationService();
        };
    }
}
