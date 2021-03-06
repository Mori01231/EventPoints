package com.github.mori01231.eventpoints.CommandExecutors;

import com.github.mori01231.eventpoints.EventPoints;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.*;

import static org.bukkit.Bukkit.getLogger;
import static org.bukkit.Bukkit.getPlayerUniqueId;

public class ListCommandExecutor implements CommandExecutor {

    public Connection connection;
    private String host, database, username, password;
    private int port;

    private String DatabaseName;
    private Boolean isConsole;
    Player player;

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if ((sender instanceof Player)) {
            player = (Player) sender;
            isConsole = false;
        } else{
            isConsole = true;
        }

        BukkitRunnable r = new BukkitRunnable() {
            @Override
            public void run() {
                //Database interaction

                try {
                    openConnection();
                    Statement statement = connection.createStatement();

                    ResultSet result = statement.executeQuery("show tables;");
                    if (result.next() == false) {
                        FeedBack("&cイベントポイントが一つも存在しません。");
                    } else {
                        FeedBack("&e&lイベントポイント");
                        FeedBack("&e------------------------");
                        FeedBack(result.getString(1));
                        while(result.next()){
                            FeedBack(result.getString(1));
                        }
                    }

                } catch(ClassNotFoundException | SQLException e) {
                    e.printStackTrace();
                }
            }
        };

        r.runTaskAsynchronously(EventPoints.getInstance());

        return true;
    }



    public void openConnection() throws SQLException, ClassNotFoundException {

        host = EventPoints.getInstance().getConfig().getString("host");
        port = EventPoints.getInstance().getConfig().getInt("port");
        database = EventPoints.getInstance().getConfig().getString("database");
        username = EventPoints.getInstance().getConfig().getString("username");
        password = EventPoints.getInstance().getConfig().getString("password");

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

    public void FeedBack(String message){
        if(isConsole){
            getLogger().info(ChatColor.translateAlternateColorCodes('&',message));
        }else{
            player.sendMessage(ChatColor.translateAlternateColorCodes('&',message));
        }
    }

}
