package ru.yandex.practicum.client.util;

import java.io.PrintWriter;
import java.io.Writer;

public class Logger extends PrintWriter {

    public Logger(Writer osw) {
        super(osw, true);
    }

    public void log(String tag, String msg) {
        println('[' + tag + ']' + ' ' + msg);
    }
}
