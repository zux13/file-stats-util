package dev.zux13.filestatistics.output;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ConsolePrinter {

    public static void printHelp() {

        String help =
            """
            Использование: java -jar <jar_name> <path> [options]

            Консольная утилита для подсчёта статистики по файлам в указанном каталоге.

            Параметры:
              <path>                     Путь до каталога по которому надо выполнить сбор статистики (обязательный).
              --recursive                Выполнять обход дерева рекурсивно.
              --max-depth=<number>       Глубина рекурсивного обхода (только если --recursive).
              --thread=<number>          Количество потоков используемого для обхода.
              --include-ext=<ext1,ext2,..>  Обрабатывать файлы только с указанными расширениями (без точки).
              --exclude-ext=<ext1,ext2,..>  Не обрабатывать файлы с указанными расширениями (без точки).
              --git-ignore               Не обрабатывать файлы указанные в файле .gitignore (опционально).
              --output=<plain,xml,json>  Формат вывода статистики (по умолчанию plain).
              --verbose                  Выводить информацию о проигнорированных файлах.
              -h, --help                 Показать это сообщение и выйти.

            Примеры:
              java -jar file-stats.jar /home/user/src
              java -jar file-stats.jar /home/user/src --recursive --include-ext=java,xml --output=json
              java -jar file-stats.jar /home/user/src --max-depth=2 --thread=4 --exclude-ext=tmp,log --verbose
            """;

        print(help);
    }

    public static void print(String output) {
        System.out.println(output);
    }
}