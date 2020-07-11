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

public class SeeCommandExecutor implements CommandExecutor {

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

        DatabaseName = args[0];
        String seePlayerUUID;
        String seePlayer;


        try {
            seePlayerUUID = getPlayerUniqueId(args[1]).toString();
            seePlayer = args[1];
        }catch(Exception e){
            FeedBack("&c" + args[1] + "という名前のプレイヤーは存在しません。");
            return true;
        }

        BukkitRunnable r = new BukkitRunnable() {
            @Override
            public void run() {
                //Database interaction

                try {
                    openConnection();
                    Statement statement = connection.createStatement();

                    ResultSet result = statement.executeQuery("SHOW TABLES LIKE '" + DatabaseName + "';");
                    if (result.next() == false) {
                        FeedBack("&c" + DatabaseName + "という名前のイベントポイントは存在しません。/epn " + DatabaseName + " コマンドで先にそのイベントポイントを作成してください。");
                    } else {
                        ResultSet findPlayer = statement.executeQuery("SELECT * FROM " + DatabaseName + " WHERE PlayerUUID = '" + seePlayerUUID + "';");
                        if (findPlayer.next() == false) {
                            FeedBack("&e現在" + seePlayer + "は" + DatabaseName + "を&e&l0&eポイント所持しています。");
                        } else {
                            Integer points = Integer.valueOf(findPlayer.getString("points"));
                            FeedBack("&e現在" + seePlayer + "は" + DatabaseName + "を&e&l" + points + "&eポイント所持しています。");
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
