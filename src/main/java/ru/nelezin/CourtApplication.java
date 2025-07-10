package ru.nelezin;

import java.util.ArrayList;
import ru.nelezin.entity.CaseRecord;
import ru.nelezin.factory.CaseRecordFactory;
import ru.nelezin.service.CaseRecordService;

import java.util.List;
import java.util.Scanner;
import java.util.Set;

/*
Суд цветочного города ведет статистику судебного делопроизводства, записывает имя ответчика, имя истца, дату, статью, итог(оправдан/осужден);
Всего известно о 6490 записях (подумайте, как можно смоделировать ввод с помощью случайных чисел)
Необходимо вывести следующую информацию:
 - Посчитать количество людей, которые участвовали в процессах, но не были осуждены
 - Найти людей, которые участвовали в процессах, более чем по 1 статье за последние 10 лет
 - Вывести людей, которые подавали в суд больше 1 раза за последние 3 года

В задаче должны использоваться коллекции (за исключением Map),
необходимо выбрать наиболее подходящую (подходящие) коллекцию для решения задачи,
аргументировать свой выбор
В задаче должен использоваться Stream API
 */
public class CourtApplication {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter a number of records: ");
        int n = scanner.nextInt();
        List<CaseRecord> records = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            records.add(CaseRecordFactory.generateRecord());
        }

        CaseRecordService caseRecordService = new CaseRecordService();

        System.out.print("Count the number of people who participated in the trials" +
                " but were not convicted: ");
        System.out.print(caseRecordService.getNotConvictedPeople(records).size());
        System.out.println('\n');

        System.out.println("Find people who participated in the processes" +
                " in more than 1 article over the past 10 years:");
        Set<String> peopleWhoParticipated = caseRecordService.getPeopleMultipleArticles(records);
        int i = 0;
        for (String name : peopleWhoParticipated) {
            if (i == peopleWhoParticipated.size() - 1) {
                System.out.print(name);
            } else {
                System.out.print(name + ", ");
            }
            i++;
        }
        System.out.println('\n');

        System.out.println("Find people who have sued more than 1 time in the last 3 years:");
        Set<String> frequentPlaintiffs = caseRecordService.getFrequentPlaintiffs(records);
        i = 0;
        for (String name : frequentPlaintiffs) {
            if (i == frequentPlaintiffs.size() - 1) {
                System.out.print(name);
            } else {
                System.out.print(name + ", ");
            }
            i++;
        }
    }
}