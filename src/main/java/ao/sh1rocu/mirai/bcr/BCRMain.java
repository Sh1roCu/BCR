/*
 * Copyright (C) 2022 Sh1roCu
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package ao.sh1rocu.mirai.bcr;

import ao.sh1rocu.mirai.bcr.event.MyGroupMessageEvent;
import ao.sh1rocu.mirai.bcr.event.MyUserMessageEvent;
import ao.sh1rocu.mirai.bcr.task.ClanBattleWatcher;
import ao.sh1rocu.mirai.bcr.task.DynamicWatcher;
import com.google.gson.Gson;
import net.mamoe.mirai.console.plugin.jvm.JavaPlugin;
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescriptionBuilder;
import net.mamoe.mirai.event.GlobalEventChannel;
import net.mamoe.mirai.event.events.BotInvitedJoinGroupRequestEvent;
import net.mamoe.mirai.event.events.GroupMessageEvent;
import net.mamoe.mirai.event.events.UserMessageEvent;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * PluginMain
 */
public final class BCRMain extends JavaPlugin {
    /**
     * The constant INSTANCE.
     */
    public static final BCRMain INSTANCE = new BCRMain();

    private BCRMain() {
        super(new JvmPluginDescriptionBuilder(
                "ao.sh1rocu.mirai.bcr",
                "1.0.1")
                .name("Sh1roCu-BCR")
                .author("Sh1roCu")
                .build());
    }

    @Override
    public void onEnable() {
        createUPerFaceCacheFile();
        createClanBattleConfigFile();
        createWatcherConfigFile();
        startWatcher();
        getLogger().info("插件加载完成...");
        GlobalEventChannel.INSTANCE.subscribeAlways(GroupMessageEvent.class, new MyGroupMessageEvent());
        GlobalEventChannel.INSTANCE.subscribeAlways(UserMessageEvent.class, new MyUserMessageEvent());
     }

    /**
     * Create UPer face cache file.
     */
    public void createUPerFaceCacheFile() {
        File file = new File(getDataFolderPath() + "\\UPerFaceCache");
        if (!file.exists()) file.mkdirs();
    }

    /**
     * Create clan battle config file.
     */
    public void createClanBattleConfigFile() {
        File clan_battle_data = new File(getConfigFolderPath() + "\\ClanBattle.json");
        if (!clan_battle_data.exists()) {
            Map<String, String> data = new LinkedHashMap<>();
            data.put("群号", "bigfun登录后的cookie");
            data.put("example", "xxxx");
            Gson gson = new Gson();
            String json = gson.toJson(data);
            try {
                Writer writer = new OutputStreamWriter(new FileOutputStream(clan_battle_data), StandardCharsets.UTF_8);
                writer.write(json);
                writer.flush();
                writer.close();
            } catch (FileNotFoundException e) {
                getLogger().error("会战配置文件创建失败");
            } catch (IOException e) {
                getLogger().error("数据写入失败");
            }
        }
    }

    /**
     * Create watcher config file.
     */
    public void createWatcherConfigFile() {
        File watcher_data = new File(getConfigFolderPath() + "\\DynamicWatcher.json");
        if (!watcher_data.exists()) {
            Map<String, Map<String, String>> data = new HashMap<>();
            Map<String, String> value = new HashMap<>();
            value.put("群号", "on/off");
            value.put("xxxx", "on");
            data.put("UID", value);
            Gson gson = new Gson();
            String json = gson.toJson(data);
            try {
                Writer writer = new OutputStreamWriter(new FileOutputStream(watcher_data), StandardCharsets.UTF_8);
                writer.write(json);
                writer.flush();
                writer.close();
            } catch (FileNotFoundException e) {
                getLogger().error("检测动态的配置文件创建失败");
            } catch (IOException e) {
                getLogger().error("数据写入失败");
            }
        }
    }

    /**
     * Start watcher.
     */
    public void startWatcher() {
        new Timer().schedule(new DynamicWatcher(), new Date(), 1);
        new Timer().schedule(new ClanBattleWatcher(), new Date(), 1);
    }
}