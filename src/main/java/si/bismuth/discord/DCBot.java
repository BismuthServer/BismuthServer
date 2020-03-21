package si.bismuth.discord;

import net.dv8tion.jda.api.AccountType;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.EmbedType;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.HoverEvent;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import si.bismuth.MCServer;

import javax.security.auth.login.LoginException;
import java.util.Arrays;
import java.util.List;

public class DCBot extends ListenerAdapter {
	private final static Logger LOGGER = LogManager.getLogger();
	private final static long BismuthID = 635252849571266580L;
	private final static long ChannelID = 635254222916419590L;
	private final static String channelURL = String.format("https://discordapp.com/channels/%d/%d/", BismuthID, ChannelID);
	private final static String PREFIX = ";";
	private final boolean isTestServer;
	private final JDA jda;

	public DCBot(String token, boolean isOnlineMode) throws LoginException {
		isTestServer = !isOnlineMode;
		this.jda = new JDABuilder(AccountType.BOT)
				.setToken(token)
				.addEventListeners(this)
				.build();
		this.sendToDiscord("Server started!");
	}

	public void sendToDiscord(String message) {
		final TextChannel channel = this.jda.getTextChannelById(DCBot.ChannelID);
		if (channel != null) {
			if (isTestServer) {
				message = "\uD83E\uDDEA" + message;
			}

			channel.sendMessage(message).queue();
		}
	}

	public void sendDeathmessage(ITextComponent component) {
		this.sendToDiscord("**\uD83D\uDD71 " + component.getUnformattedText() + "**");
	}

	@Override
	public void onMessageReceived(MessageReceivedEvent event) {
		if (event.getMember() == null || event.getAuthor().isBot() || event.getGuild().getIdLong() != BismuthID) {
			return;
		}

		// TODO: handle DC2MC images
		final MessageChannel channel = event.getChannel();
		final Member member = event.getMember();
		final Message message = event.getMessage();
		final String content = message.getContentDisplay();
		final List<Message.Attachment> attachments = message.getAttachments();
		if (channel.getIdLong() == ChannelID && !content.startsWith(PREFIX)) {
			final String name = event.getMember().getEffectiveName();
			final ITextComponent symbol = new TextComponentString("\u24B9");
			final HoverEvent hoverText = new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponentString(member.getUser().getAsTag()));
			final ClickEvent clickText = new ClickEvent(ClickEvent.Action.OPEN_URL, channelURL);
			symbol.getStyle().setColor(TextFormatting.BLUE).setHoverEvent(hoverText).setClickEvent(clickText);
			final ITextComponent text = new TextComponentTranslation("chat.type.text", name, content);
			for (Message.Attachment file : attachments) {
				final String url = file.getUrl();
				final ITextComponent fileUrl = new TextComponentString(" " + url);
				fileUrl.getStyle().setColor(TextFormatting.BLUE).setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, url));
				text.appendSibling(fileUrl);
			}

			MCServer.server.addScheduledTask(() -> MCServer.server.getPlayerList().sendMessage(new TextComponentString("").appendSibling(symbol).appendSibling(text)));
			return;
		}

		final String[] args = content.split(" ");
		if (args.length == 0) {
			return;
		}

		final String command = args[0];

		if (this.isCommand(command, new String[]{"lazy"})) {
			MCServer.server.addScheduledTask(() -> channel.sendMessage("no u").queue());
		}

		if (this.isCommand(command, new String[]{"tps"})) {
			MCServer.server.addScheduledTask(() -> {
				final double MSPT = MathHelper.average(MCServer.server.tickTimeArray) * 1E-6D;
				final double TPS = 1000D / Math.max(50, MSPT);
				channel.sendMessage(String.format("**TPS: %.2f MSPT: %.2f**", TPS, MSPT)).queue();
			});
		}

		if (this.isCommand(command, new String[]{"list", "players", "online"})) {
			MCServer.server.addScheduledTask(() -> {
				final String[] players = MCServer.server.getOnlinePlayerNames();
				final String title = String.format("%d player%s online:", players.length, players.length != 1 ? "s" : "");
				final MessageEmbed.AuthorInfo author = new MessageEmbed.AuthorInfo("BismuthBot", null, "https://i.imgur.com/a2w3DjI.png", null);
				final MessageEmbed embed = new MessageEmbed(null, title, StringUtils.join(players, "\n").replaceAll("_", "\\\\_"), EmbedType.RICH, null, 0x8665BD, null, null, author, null, null, null, null);
				channel.sendMessage(embed).queue();
			});
		}
	}

	private boolean isCommand(String text, String[] command) {
		return Arrays.stream(command).anyMatch(s -> text.startsWith(PREFIX + s));
	}
}
