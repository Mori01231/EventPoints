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
        // Get the database name
        DatabaseName = args[0];

        //get display name of event point ticket
        DisplayName = "§b" + DatabaseName;

        //Convert any valid ConvertModes into one ConvertMode
        ConvertMode = 0;
        if (args[1].equalsIgnoreCase("item") || args[1].equalsIgnoreCase("items") || args[1].equalsIgnoreCase("i")){
            ConvertMode = 1;
        }
        if (args[1].equalsIgnoreCase("points") || args[1].equalsIgnoreCase("point") || args[1].equalsIgnoreCase("p")){
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

        new BukkitRunnable() {
            @Override
            public void run() {
                // database interaction
                try {
                    // connect
                    openConnection();
                    Statement statement = connection.createStatement();

                    // check to see if database exists
                    ResultSet result = statement.executeQuery("SHOW TABLES LIKE '" + DatabaseName + "';");
                    if (result.next() == false) {
                        FeedBack("&c" + DatabaseName + "という名前のイベントポイントは存在しません。/epn " + DatabaseName + " コマンドで先にそのイベントポイントを作成してください。");
                    } else {
                        //Check to see if player is already registered.
                        ResultSet findPlayer = statement.executeQuery("SELECT * FROM " + DatabaseName + " WHERE PlayerUUID = '" + PlayerUUID + "';");
                        if (findPlayer.next() == false) {
                            // Register player.
                            statement.executeUpdate("INSERT INTO " + DatabaseName + " (PlayerUUID, points) VALUES ('" + PlayerUUID + "', '0');");
                            currentPoints = 0;
                        } else {
                            // Return value of points
                            currentPoints = Integer.valueOf(findPlayer.getString("points"));
                        }


                        //Actual conversion

                        //Convert items to points
                        if (ConvertMode == 1){
                            int ConvertedItems = ConvertEventPoints(player, currentPoints, ConvertMode, convertAmount, DatabaseName, DisplayName);
                            // Errors
                            if (ConvertedItems < 0){
                                if(ConvertedItems == -1){
                                    FeedBack("&c&lオンタイムチケットが足りません。" + convertAmount + "枚以上のオンタイムチケットをインベントリに入れてください。");
                                }
                            }
                            else{
                                Integer addPoints = ConvertedItems;
                                Integer points = currentPoints;
                                points = points + addPoints;
                                statement.executeUpdate("DELETE FROM `" + DatabaseName + "` WHERE PlayerUUID = '" + PlayerUUID + "';");
                                statement.executeUpdate("INSERT INTO " + DatabaseName + " (PlayerUUID, points) VALUES ('" + PlayerUUID + "', '" + points + "');");
                                FeedBack("&e" + PlayerName + "に" + DatabaseName + "を&e&l" + addPoints + "&eポイント付与しました。");
                                FeedBack("&e現在" + PlayerName + "は" + DatabaseName + "を合計&e&l" + points + "&eポイント所持しています。");
                            }
                        }

                        //Convert points to items
                        if (ConvertMode == 2){


                            // number of items to give
                            int giveItems;
                            if (convertAmount == -1){
                                giveItems = currentPoints;
                            }
                            else if (currentPoints >= convertAmount){
                                giveItems = convertAmount;
                            }
                            else{
                                giveItems = -1;
                                FeedBack("&cポイントが足りません");
                            }
                            if(AvailableSlots(player) < (giveItems - giveItems % 64)){
                                FeedBack("&cインベントリにスペースが足りません");
                                giveItems = -1;
                            }

                            String MMItemName = EventPoints.getInstance().getConfig().getString( "MMItemName");

                            if(giveItems > 0){
                                Integer points = currentPoints;
                                points = points - giveItems;
                                statement.executeUpdate("DELETE FROM `" + DatabaseName + "` WHERE PlayerUUID = '" + PlayerUUID + "';");
                                statement.executeUpdate("INSERT INTO " + DatabaseName + " (PlayerUUID, points) VALUES ('" + PlayerUUID + "', '" + points + "');");
                            }

                            int finalGiveItems = giveItems;
                            new BukkitRunnable() {
                                @Override
                                public void run() {
                                    if(finalGiveItems > 0){
                                        getServer().dispatchCommand(getServer().getConsoleSender(), "mm i give " + PlayerName + " " + MMItemName + " " + finalGiveItems);
                                        getLogger().info(PlayerName + "にMMアイテム " + MMItemName + " を " + finalGiveItems + " 個与えました。");
                                    }
                                }
                            }.runTask(EventPoints.getInstance());
                        }
                    }

                } catch(ClassNotFoundException | SQLException e) {
                    e.printStackTrace();
                }
            }
        }.runTaskAsynchronously(EventPoints.getInstance());

        return true;
    }



    public Integer ConvertEventPoints(Player player, int hasPoints, int mode, int convertNumber, String dbName, String itemDisplayName){

        //Initializing the message to be sent to the player
        String ReturnMessage;

        // item mode
        if (mode == 1) {

            // get player inventory.
            Inventory inv = player.getInventory();

            //initializing counter for tickets and points.
            int points = 0;
            int initialtickets = 0;
            int UnconvertedTickets = 0;


            initialtickets = Tickets(player, itemDisplayName);
            if (convertNumber == -1) {
                convertNumber = initialtickets;
            }
            UnconvertedTickets = convertNumber;

            if (initialtickets < UnconvertedTickets) {
                //Create the message to be sent to the player
                ReturnMessage = "&c&lオンタイムチケットが足りません。" + convertNumber + "枚以上のオンタイムチケットをインベントリに入れてください。";

                //Return the message to be sent to the player
                return -1;
            }

            //counting and deleting tickets.
            for (ItemStack item : inv.getContents()) {
                try {
                    if (item.getItemMeta().getDisplayName().equals(itemDisplayName)) {
                        //Full stack can be converted
                        if (item.getAmount() <= UnconvertedTickets) {
                            UnconvertedTickets -= item.getAmount();
                            item.setAmount(0);
                        }
                        //Only part of stack can be converted
                        else {
                            item.setAmount(item.getAmount() - UnconvertedTickets);
                            UnconvertedTickets = 0;
                        }
                    }

                } catch (Exception e) {
                }
            }

            points = convertNumber;

            //Give player ontime points.
            //getServer().dispatchCommand(getServer().getConsoleSender(), "points give " + player.getName() + " " + points);

            //Create the message to be sent to the player
            ReturnMessage = "&bオンタイムチケット" + convertNumber + "枚をオンタイムポイント" + points + "ポイントに変換しました。";
            // Send message to player
            return convertNumber;
        }

        else{
            return -2;
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

    //get amount of available slots in player inventory excluding armor and offhand
    public int Tickets(Player player, String displayName){

        //get display name of ontime ticket


        //get player inventory.
        Inventory inv = player.getInventory();

        //initializing counter for slots.
        int tickets=0;

        //counting the number of available slots.
        for (ItemStack item: inv.getContents()) {
            try{
                if(item.getItemMeta().getDisplayName().equals(displayName)){
                    tickets += item.getAmount();
                }

            }catch (Exception e){
            }
        }


        //return the number of available slots.
        return tickets;
    }
    public void FeedBack(String message){
        player.sendMessage(ChatColor.translateAlternateColorCodes('&',message));
    }
}
