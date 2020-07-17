package com.github.mori01231.eventpoints.CommandExecutors;

import com.github.mori01231.eventpoints.EventPoints;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import javax.xml.crypto.Data;
import java.sql.*;

import static org.bukkit.Bukkit.getLogger;
import static org.bukkit.Bukkit.getServer;

public class ConvertCommandExecutor implements CommandExecutor {

    public Connection connection;
    private String host, database, username, password;
    private int port;

    private String DatabaseName;
    private String DisplayName;
    private int ConvertMode;
    private int currentPoints;
    private Boolean AmountIsIndicated = true;
    Player player;

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {



        //Check if person executing command is player
        if ((sender instanceof Player)) {
            player = (Player) sender;
        } else{
            getLogger().info(ChatColor.translateAlternateColorCodes('&',"このコマンドはコンソールからは実行できません。"));
            return true;
        }

        //Not enough arguments
        if(args.length == 0){
            FeedBack("&c作成するイベントポイントの名前を指定してください 例：/epc lgw_summer_2020 item");
            return true;
        }
        if(args.length == 1){
            FeedBack("&cポイントに変換するかアイテムに変換するか指定してください 例：/epc lgw_summer_2020 points");
            return true;
        }
        if(args.length == 2){
            AmountIsIndicated = false;
        }

        String PlayerName = player.getName();
        String PlayerUUID = String.valueOf(player.getUniqueId());

        DatabaseName = args[0];

        //get display name of event point ticket
        DisplayName = "§b" + DatabaseName;

        //Convert any valid ConvertModes into one ConvertMode
        ConvertMode = 0;
        if (args[1] == "item" || args[1] == "items" || args[1] == "i"){
            ConvertMode = 1;
        }
        if (args[1] == "points" || args[1] == "point" || args[1] == "p"){
            ConvertMode = 2;
        }

        //Invalid ConvertMode
        if (ConvertMode == 0){
            FeedBack("&cイベントポイントをアイテムに変換する場合は変換方式にitemを指定してください 例：/epc lgw_summer_2020 item");
            FeedBack("&cアイテムをイベントポイントに変換する場合は変換方式にpointsを指定してください 例：/epc lgw_summer_2020 points");
            return true;
        }

        int convertAmount;
        if (AmountIsIndicated){
            try {
                convertAmount = Integer.parseInt(args[2]);
                if (convertAmount < 0){
                    FeedBack("&c&l変換するアイテム数は正の整数で指定してください");
                    return true;
                }
            }catch(Exception e){
                FeedBack("&c&l変換するアイテム数は正の整数で指定してください");
                return true;
            }
        }else{
            convertAmount = -1;
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
                        ResultSet findPlayer = statement.executeQuery("SELECT * FROM " + DatabaseName + " WHERE PlayerUUID = '" + PlayerUUID + "';");
                        if (findPlayer.next() == false) {
                            currentPoints = 0;
                        } else {
                            Integer currentPoints = Integer.valueOf(findPlayer.getString("points"));
                        }
                        ConvertEventPoints(player, currentPoints, ConvertMode, convertAmount);
                    }

                } catch(ClassNotFoundException | SQLException e) {
                    e.printStackTrace();
                }
            }
        };

        r.runTaskAsynchronously(EventPoints.getInstance());

        FeedBack("&aYou have " + currentPoints + " points.");

        return true;
    }



    public void ConvertEventPoints(Player player, int hasPoints, int mode, int convertPoints){

        // item mode
        if (mode == 1){

            // get player inventory.
            Inventory inv = player.getInventory();

            // initializing counter for tickets and points.
            int tickets = 0;
            int points = 0;

            //counting and deleting tickets.
            for (ItemStack item: inv.getContents()) {
                try{
                    if(item.getItemMeta().getDisplayName().equals(DisplayName)){
                        tickets += item.getAmount();
                        item.setAmount(0);
                    }

                }catch (Exception e){
                }
            }

            points = tickets;

            //Give player ontime points.
            getServer().dispatchCommand(getServer().getConsoleSender(), "epa " + DatabaseName + " " + player.getName() + " " + points);

            //Create the message to be sent to the player
            String ReturnMessage = "&b" + DatabaseName + "チケット" + tickets + "枚を" + DatabaseName + "ポイント" + points + "ポイントに変換しました。";

            // Send message to player
            FeedBack(ReturnMessage);
        }

        // points mode
        if (mode == 2){

        }

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

    //get amount of available slots in player inventory excluding armor and offhand
    public int AvailableSlots(Player player){
        //get player inventory.
        Inventory inv = player.getInventory();

        //initializing counter for slots.
        int slots=0;

        //counting the number of available slots.
        for (ItemStack item: inv.getContents()) {
            if(item == null) {
                slots++;
            }
        }

        //Compensating for empty offhand slot
        if(player.getInventory().getItemInOffHand().getType() == Material.AIR){
            slots--;
        }

        //Compensating for empty armor slots
        for (ItemStack item: player.getInventory().getArmorContents()){
            if(item == null) {
                slots--;
            }
        }

        //return the number of available slots.
        return slots;
    }

    public void FeedBack(String message){
        player.sendMessage(ChatColor.translateAlternateColorCodes('&',message));
    }
}
