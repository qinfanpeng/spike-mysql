package com.tw.spik.mysq;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.counting;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;

public class AlarmServiceTest {

    @Test
    public void test_calcAlarmWindowNos() {
        final AlarmService alarmService = new AlarmService();
        final List<Alarm> alarms = Arrays.asList(
            new Alarm("a1", "n1", 1),
            new Alarm("a1", "n1", 9),
            new Alarm("a1", "n2", 22),
            new Alarm("a1", "n2", 25),
            new Alarm("a2", "n1", 23),
            new Alarm("a2", "n2", 55)
        );
        final Map<String, Map<Long, List<Alarm>>> nameWindowNoAlarmsMap = alarmService.calcAlarmWindowNos(alarms);

        final Map<Long, Integer> windowNoAlarmCountMap1 = alarmService.getWindowNoAlarmCountMap("a1", nameWindowNoAlarmsMap);
        final Map<Long, Integer> windowNoAlarmCountMap2 = alarmService.getWindowNoAlarmCountMap("a2", nameWindowNoAlarmsMap);


        final Set<Long> windowNos1 = windowNoAlarmCountMap1.keySet();
        final Set<Long> windowNos2 = windowNoAlarmCountMap2.keySet();
        final List<Long> allWindowNos = Stream.concat(windowNos1.stream(), windowNos2.stream()).collect(toList());

        List<Integer> alarmCountVector1 = allWindowNos.stream()
            .map(windowNo -> windowNoAlarmCountMap1.getOrDefault(windowNo, 0))
            .collect(toList());

        List<Integer> alarmCountVector2 = allWindowNos.stream()
            .map(windowNo -> windowNoAlarmCountMap2.getOrDefault(windowNo, 0))
            .collect(toList());

        // ---------------------------------

        final Map<Long, Map<String, Long>> windowNoNodeMinFirstOccurMap1 = alarmService.getWindowNoNodeMinFirstOccurMap("a1", nameWindowNoAlarmsMap);
        final Map<Long, Map<String, Long>> windowNoNodeMinFirstOccurMap2 = alarmService.getWindowNoNodeMinFirstOccurMap("a2", nameWindowNoAlarmsMap);

        final Stream<Long> bothHaveWindowNos = windowNos1.stream().filter(windowNos2::contains);

        final Map<Boolean, Long> timeSeriesResult = bothHaveWindowNos
            .flatMap(windowNo -> {
                final Map<String, Long> nodeMinFirstOccurMap1 = windowNoNodeMinFirstOccurMap1.get(windowNo);
                final Map<String, Long> nodeMinFirstOccurMap2 = windowNoNodeMinFirstOccurMap2.get(windowNo);
                final Set<String> nodes1 = nodeMinFirstOccurMap1.keySet();
                final Set<String> nodes2 = nodeMinFirstOccurMap2.keySet();

                final Stream<String> bothHaveNodes = nodes1.stream().filter(nodes2::contains);

                return bothHaveNodes
                    .map(node -> {
                        return nodeMinFirstOccurMap1.get(node) < nodeMinFirstOccurMap2.get(node);
                    });
            })
            .collect(groupingBy(identity(), counting()));

        final Long trueCount = timeSeriesResult.getOrDefault(true, 0L);
        final Long falseCount = timeSeriesResult.getOrDefault(false, 0L);
    }
}