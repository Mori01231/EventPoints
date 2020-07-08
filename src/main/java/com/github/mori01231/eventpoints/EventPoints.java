package com.github.mori01231.eventpoints;

import org.bukkit.plugin.java.JavaPlugin;

import java.sql.Connection;

public final class EventPoints extends JavaPlugin {

    private Connection connection;
    private String host, database, username, password;
    private int port;

    @Override
    public void onEnable() {
        // Plugin startup logic
        this.saveDefaultConfig();


        host = getConfig().getString("host");
        port = getConfig().getInt("port");
        database = getConfig().getString("database");
        username = getConfig().getString("username");
        password = getConfig().getString("Password");
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
