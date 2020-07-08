package com.github.mori01231.eventpoints.CommandExecutors;

import com.github.mori01231.eventpoints.EventPoints;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import static org.bukkit.Bukkit.getLogger;


public class CreateCommandExecutor implements CommandExecutor {

    public Connection connection;
    private String host, database, username, password;
    private int port;

    private String DatabaseName;
    private Boolean isConsole;
    Player player;

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        DatabaseName = args[0];
        if ((sender instanceof Player)) {
            player = (Player) sender;
            isConsole = false;
        }
        else{
            isConsole = true;
        }

        r.runTaskAsynchronously(EventPoints.getInstance());

        return true;
    }

    BukkitRunnable r = new BukkitRunnable() {
        @Override
        public void run() {
            //This is where you should do your database interaction

            try {
                openConnection();
                Statement statement = connection.createStatement();

                ResultSet result = statement.executeQuery("SHOW TABLES LIKE '" + DatabaseName + "';");
                if (result.next() == false) {
                    FeedBack("&e" + DatabaseName + "という名前のイベントポイントは現在存在しません。新たに" + DatabaseName + "イベントポイントを作成します。");
                    statement.executeUpdate("CREATE TABLE IF NOT EXISTS `" + DatabaseName + "` (`PlayerUUID` varchar(36), `points` int)");
                } else {
                    FeedBack("&c" + DatabaseName + "という名前のイベントポイントはすでに存在します。別の名前のイベントポイントを作成するか、一度このイベントポイントを削除し、再度作ってください。");
                }

            } catch(ClassNotFoundException | SQLException e) {
                e.printStackTrace();
            }
        }
    };

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
