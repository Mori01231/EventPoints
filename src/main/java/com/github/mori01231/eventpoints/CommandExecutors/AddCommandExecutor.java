package com.github.mori01231.eventpoints.CommandExecutors;

import com.github.mori01231.eventpoints.EventPoints;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.*;

import static org.bukkit.Bukkit.*;

public class AddCommandExecutor implements CommandExecutor {

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


        if(args.length == 0){
            FeedBack("&cイベントポイントの種類を指定してください");
            return true;
        }
        if(args.length == 1){
            FeedBack("&cプレイヤーを指定してください");
            return true;
        }
        if(args.length == 2){
            FeedBack("&c追加するポイント数を指定してください");
            return true;
        }
        DatabaseName = args[0];
        String addPlayerUUID;
        String addPlayer;
        Integer addPoints;

        try {
            addPlayerUUID = getPlayerUniqueId(args[1]).toString();
            addPlayer = args[1];
        }catch(Exception e){
            FeedBack("&c" + args[1] + "という名前のプレイヤーは存在しません。");
            return true;
        }

        try {
            addPoints = Integer.valueOf(args[2]);
        }catch(Exception e){
            FeedBack("&c追加するポイントは整数で指定してください。");
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
                        FeedBack("&c" + DatabaseName + "という名前のイベントポイントは存在しません。/epn " + DatabaseName + " コマンドで先にそのイベントポイントを作成してください。");
                    } else {
                        ResultSet findPlayer = statement.executeQuery("SELECT * FROM " + DatabaseName + " WHERE PlayerUUID = '" + addPlayerUUID + "';");
                        if (findPlayer.next() == false) {
                            statement.executeUpdate("INSERT INTO " + DatabaseName + " (PlayerUUID, points) VALUES ('" + addPlayerUUID + "', '" + addPoints + "');");
                            FeedBack("&e" + addPlayer + "に" + DatabaseName + "を" + addPoints + "ポイント付与しました。");
                            FeedBack("&e現在" + addPlayer + "は" + DatabaseName + "を合計&e&l" + addPoints + "&eポイント所持しています。");
                        } else {
                            Integer points = Integer.valueOf(findPlayer.getString("points"));
                            points = points + addPoints;
                            statement.executeUpdate("DELETE FROM `" + DatabaseName + "` WHERE PlayerUUID = '" + addPlayerUUID + "';");
                            statement.executeUpdate("INSERT INTO " + DatabaseName + " (PlayerUUID, points) VALUES ('" + addPlayerUUID + "', '" + points + "');");
                            FeedBack("&e" + addPlayer + "に" + DatabaseName + "を&e&l" + addPoints + "&eポイント付与しました。");
                            FeedBack("&e現在" + addPlayer + "は" + DatabaseName + "を合計&e&l" + points + "&eポイント所持しています。");
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