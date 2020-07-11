package com.github.mori01231.eventpoints;

import com.github.mori01231.eventpoints.CommandExecutors.*;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public final class EventPoints extends JavaPlugin {

    private static EventPoints instance;
    public EventPoints (){
        instance = this;
    }
    public static EventPoints getInstance() {
        return instance;
    }

    public Connection connection;
    private String host, database, username, password;
    private int port;


    @Override
    public void onEnable() {
        // Plugin startup logic

        //Assign command executors
        try{
            this.getCommand("eventpointnew").setExecutor(new NewCommandExecutor());
            this.getCommand("eventpointdelete").setExecutor(new DeleteCommandExecutor());
            this.getCommand("eventpointadd").setExecutor(new AddCommandExecutor());
            this.getCommand("eventpointsee").setExecutor(new SeeCommandExecutor());
            this.getCommand("eventpointlist").setExecutor(new ListCommandExecutor());
            this.getCommand("eventpointconvert").setExecutor(new ConvertCommandExecutor());
        }catch(NullPointerException e){
            getLogger().info("Command Executor does not exist");
        }


        this.saveDefaultConfig();

        host = getConfig().getString("host");
        port = getConfig().getInt("port");
        database = getConfig().getString("database");
        username = getConfig().getString("username");
        password = getConfig().getString("password");

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        try { //using a try catch to catch connection errors (like wrong sql password...)
            if (connection!=null && !connection.isClosed()){ //checking if connection isn't null to
                //avoid receiving a nullpointer
                connection.close(); //closing the connection field variable.
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

}
