package com.tw.spik.mysq;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import static java.util.Collections.max;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

public class AlarmService {

    public Map<String, Map<Long, List<Alarm>>> calcAlarmWindowNos(List<Alarm> alarms) {
        alarms.forEach(Alarm::setWindowNo);


        final Map<String, Map<Long, List<Alarm>>> nameWindowNoAlarmsMap = alarms.stream()
            .collect(groupingBy(Alarm::name))
            .entrySet().stream()
            .collect(toMap(
                Entry::getKey,
                entry -> {
                    return entry.getValue().stream()
                        .collect(groupingBy(Alarm::windowNo));
                })
            );

        return nameWindowNoAlarmsMap;
    }

    public Map<Long, Integer> getWindowNoAlarmCountMap(String name, Map<String, Map<Long, List<Alarm>>> nameWindowNoAlarmsMap) {
        final Map<Long, List<Alarm>> windowNoAlarmsMap = nameWindowNoAlarmsMap.get(name);
        return windowNoAlarmsMap.entrySet().stream()
            .collect(toMap(
                Entry::getKey,
                entry -> {
                    return entry.getValue().size();
                }));
    }

    public Map<Long, Map<String, Long>> getWindowNoNodeMinFirstOccurMap(String name, Map<String, Map<Long, List<Alarm>>> nameWindowNoAlarmsMap) {
        final Map<Long, List<Alarm>> windowNoAlarmsMap = nameWindowNoAlarmsMap.get(name);

        return windowNoAlarmsMap.entrySet().stream()
            .collect(toMap(Entry::getKey, windowNoEntry -> {
                    return windowNoEntry.getValue().stream()
                        .collect(groupingBy(Alarm::node))
                        .entrySet().stream()
                        .collect(toMap(Entry::getKey, nodeEntry -> {
                                return max(
                                    nodeEntry.getValue().stream()
                                    .map(Alarm::firstOccur)
                                    .collect(toList())
                                );
                        }));
            }));
    }

}
