package tasks;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;

public class Deadline extends Task {

    protected LocalDate by;

    public Deadline(String description, LocalDate by) {
        super(description);
        this.by = by;
    }

    @Override
    public String toString() {
        String deadlineBy = DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL).format(by);
        return "[D]" + super.toString() + " (by: " + deadlineBy + ")" + "\n";
    }
}