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

package ao.sh1rocu.mirai.bcr.task;

import java.util.TimerTask;

/**
 * 监听公会战
 * @author Sh1roCu
 */
public class ClanBattleWatcher extends TimerTask {
    /**
     * 暂存boss信息的时间戳
     */
    static long clan_battle_timestamp = 0;

    @Override
    public void run() {
        /*TODO:自动报刀 bigfun接口*/
    }
}
