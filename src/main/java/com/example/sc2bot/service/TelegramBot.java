package com.example.sc2bot.service;
import com.example.sc2bot.config.BotConfig;
import com.example.sc2bot.model.User;
import com.example.sc2bot.model.UserRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import static java.lang.System.*;

@Slf4j
@Component
public class TelegramBot extends TelegramLongPollingBot {
    @Autowired
    private  UserRepository userRepository;
    final BotConfig config;
    static final String HELP_TEXT="How can I help you?\n\n" +
            "Select from the list of commands.";
    public TelegramBot(BotConfig config){
        this.config =config;
        List<BotCommand> listofCommands = new ArrayList();
        listofCommands.add(new BotCommand("/start", "get welcome message"));
        listofCommands.add(new BotCommand("/mydata", "get your data stored"));
        listofCommands.add(new BotCommand("/deletedata", "delet my data"));
        listofCommands.add(new BotCommand("/help", "info how to use this bot "));
        listofCommands.add(new BotCommand("/settings", "set your preferences"));
        try{
            this.execute(new SetMyCommands(listofCommands,new BotCommandScopeDefault(),null));
        }
        catch (TelegramApiException e){
            log.error("Error setting  bot command list: " + e.getMessage());
        }



    }
    @Override
    public String getBotUsername() {
        return config.getBotName();
    }

    @Override
    public String getBotToken() {
        return config.getToken();
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            long chatId=update.getMessage().getChatId();
            switch (messageText) {
                case "/start":

                    registerUser(update.getMessage());
                    startCommandReceived(chatId, update.getMessage().getChat().getFirstName());
                    break;
                case "/help":
                    sendMessage(chatId, HELP_TEXT);
                    break;
                default: sendMessage(chatId, "Sorry, command was not recognized");

            }
        }
    }

    private void registerUser(Message msg) {
        if(userRepository.findById(msg.getChatId()).isEmpty()) {
            var chatId = msg.getChatId();
            var chat = msg.getChat();

            User user = new User();

            user.setChatId(chatId);
            user.setFirstName(chat.getFirstName());
            user.setLastname(chat.getLastName());
            user.setUsername(chat.getUserName());
            user.setRegisteredAt(new Timestamp(System.currentTimeMillis()));

            userRepository.save(user);
            log.info("user saved: " + user );
        }
    }

    private void startCommandReceived(long chatId, String name ){
        String answer = "Hi, " + name + ", nice to meet you!";
        log.info("Replied to user " + name);
        sendMessage(chatId,answer);

        }
        private void  sendMessage (long chatId, String textToSend){
            SendMessage message = new SendMessage();
            message.setChatId(String.valueOf(chatId));
            message.setText(textToSend);

            try {
                execute(message);
            } catch (TelegramApiException e) {
                log.error("Error occurred: " + e.getMessage());

            }


        }

    }


