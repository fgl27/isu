/*
 * Copyright (C) 2016 Felipe de Leon
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.bhb27.isu;

public interface Constants {

    String PREF_NAME = "pref";

    // Battery values works for quark and maybe other Moto devices
    String BATTERY_PARAMETERS = "/sys/class/power_supply/battery";
    // Battery charging current
    String BATTERY_CHARGING_CURRENT = BATTERY_PARAMETERS + "/current_avg";
    // Battery charging mode or rate type
    String BATTERY_CHARGING_TYPE = BATTERY_PARAMETERS + "/charge_rate";
    // Battery health
    String BATTERY_HEALTH = BATTERY_PARAMETERS + "/health";
    // Battery %
    String BATTERY_CAPACITY = BATTERY_PARAMETERS + "/capacity";
}
