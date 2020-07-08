package com.github.mori01231.eventpoints;

import com.github.mori01231.eventpoints.CommandExecutors.AddCommandExecutor;
import com.github.mori01231.eventpoints.CommandExecutors.CreateCommandExecutor;
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
            this.getCommand("eventpointcreate").setExecutor(new CreateCommandExecutor());
            this.getCommand("eventpointadd").setExecutor(new AddCommandExecutor());
        }catch(NullPointerException e){
            getLogger().info("Command Executor does not exist");
        }


        this.saveDefaultConfig();

        host = getConfig().getString("host");
        port = getConfig().getInt("port");
        database = getConfig().getString("database");
        username = getConfig().getString("username");
        password = getConfig().getString("password");



        try {
            openConnection();
            Statement statement = connection.createStatement();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public void openConnection() throws SQLException, ClassNotFoundException {
        if (connection != null && !connection.isClosed()) {
            return;
        }

        synchronized (this) {
            if (connection != null && !connection.isClosed()) {
                return;
            }
            Class.forName("com.mysql.jdbc.Driver");
            connection = DriverManager.getConnection("jdbc:mysql://" + this.host + ":" + this.port + "/" + this.database, this.username, this.password);
        }
    }
}
