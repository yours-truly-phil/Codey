package io.horrorshow.codey.util;

import ch.qos.logback.classic.PatternLayout;
import ch.qos.logback.classic.pattern.ClassicConverter;
import ch.qos.logback.classic.pattern.DateConverter;
import ch.qos.logback.classic.pattern.LevelConverter;
import ch.qos.logback.classic.pattern.LoggerConverter;
import ch.qos.logback.classic.pattern.MDCConverter;
import ch.qos.logback.classic.pattern.MessageConverter;
import ch.qos.logback.classic.pattern.ThreadConverter;
import ch.qos.logback.classic.pattern.ThrowableProxyConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.CoreConstants;
import ch.qos.logback.core.pattern.FormatInfo;

import java.util.List;


public class LogbackConverter extends ClassicConverter {

    public static final String MDC_TASK_KEY = "taskId";
    public static final String MDC_GUILD_KEY = "guild";
    public static final String MDC_USER_KEY = "user";
    public static final String MDC_CHANNEL_KEY = "channel";

    private DateConverter dateConverter;
    private LevelConverter levelConverter;
    private ThreadConverter threadConverter;
    private LoggerConverter loggerConverter;
    private MDCConverter taskIdConverter;
    private MDCConverter guildConverter;
    private MDCConverter userConverter;
    private MDCConverter channelConverter;
    private MessageConverter messageConverter;
    private ThrowableProxyConverter throwableProxyConverter;


    @Override
    public void start() {
        this.dateConverter = new DateConverter();
        this.dateConverter.setOptionList(List.of("yyyy-MM-dd HH:mm:ss.SSS"));
        this.dateConverter.start();

        this.levelConverter = new LevelConverter();
        this.levelConverter.setFormattingInfo(new FormatInfo(5, 5));
        this.levelConverter.start();

        this.threadConverter = new ThreadConverter();
        this.threadConverter.setFormattingInfo(new FormatInfo(15, 15));
        this.threadConverter.start();

        this.loggerConverter = new LoggerConverter();
        this.loggerConverter.setFormattingInfo(new FormatInfo(40, 40, false, true));
        this.loggerConverter.setOptionList(List.of("39"));
        this.loggerConverter.start();

        this.taskIdConverter = new MDCConverter();
        this.taskIdConverter.setFormattingInfo(new FormatInfo(22, 22));
        this.taskIdConverter.setOptionList(List.of(MDC_TASK_KEY));
        this.taskIdConverter.start();

        this.guildConverter = new MDCConverter();
        this.guildConverter.setFormattingInfo(new FormatInfo(40, 40, false, false));
        this.guildConverter.setOptionList(List.of(MDC_GUILD_KEY));
        this.guildConverter.start();

        this.userConverter = new MDCConverter();
        this.userConverter.setFormattingInfo(new FormatInfo(40, 40, false, false));
        this.userConverter.setOptionList(List.of(MDC_USER_KEY));
        this.userConverter.start();

        this.channelConverter = new MDCConverter();
        this.channelConverter.setFormattingInfo(new FormatInfo(40, 40, false, false));
        this.channelConverter.setOptionList(List.of(MDC_CHANNEL_KEY));
        this.channelConverter.start();

        this.messageConverter = new MessageConverter();
        this.messageConverter.start();

        this.throwableProxyConverter = new ThrowableProxyConverter();
        this.throwableProxyConverter.start();

        super.start();
    }


    @Override
    public String convert(ILoggingEvent event) {
        var message = this.messageConverter.convert(event);
        var stacktrace = this.throwableProxyConverter.convert(event);
        var prefix = this.createPrefix(event);

        var sb = new StringBuilder(128);

        var messageParts = message.split(CoreConstants.LINE_SEPARATOR);
        for (var part : messageParts) {
            sb.append(prefix);
            sb.append(part);
            sb.append(CoreConstants.LINE_SEPARATOR);
        }

        if (!stacktrace.isEmpty()) {
            var stackParts = stacktrace.split(CoreConstants.LINE_SEPARATOR);
            for (var part : stackParts) {
                sb.append(prefix);
                sb.append("  ");
                sb.append(part);
                sb.append(CoreConstants.LINE_SEPARATOR);
            }
        }

        return sb.toString();
    }


    private String createPrefix(ILoggingEvent event) {
        var sb = new StringBuilder();
        this.dateConverter.write(sb, event);
        sb.append(" ");
        this.levelConverter.write(sb, event);
        sb.append(" [");
        this.threadConverter.write(sb, event);
        sb.append("] ");
        this.loggerConverter.write(sb, event);
        sb.append("[");
        this.taskIdConverter.write(sb, event);
        sb.append("] [");
        this.guildConverter.write(sb, event);
        sb.append("] [");
        this.userConverter.write(sb, event);
        sb.append("] [");
        this.channelConverter.write(sb, event);
        sb.append("] : ");
        return sb.toString();
    }


    public static void register() {
        PatternLayout.defaultConverterMap.put("codey", LogbackConverter.class.getName());
    }
}

