package commands;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import exceptions.DukeException;
import exceptions.DukeInvalidParameterException;
import exceptions.DukeUnrecognisedCommandException;
import tasks.Deadline;
import tasks.Event;
import tasks.Task;
import tasks.TaskList;
import tasks.Todo;


/**
 * The enum of all valid Commands
 */
public enum Command {
    LIST {
        @Override
        public String execute(HashMap<String, String> parameters, TaskList tasks) throws DukeInvalidParameterException {
            if (!parameters.isEmpty()) {
                throw new DukeInvalidParameterException(strings.getString("error.list"), parameters);
            }

            System.out.println(tasks);

            return IntStream.range(0, tasks.size())
                    .mapToObj(index -> (index + 1) + ". " + tasks.get(index))
                    .reduce("", (x, y) -> x + y)
                    .strip();
        }
    },
    /**
     * The Done.
     */
    DONE {
        @Override
        public String execute(HashMap<String, String> parameters, TaskList tasks) throws DukeException {
            if (!parameters.containsKey("argument")) {
                throw new DukeInvalidParameterException(strings.getString("error.done"), parameters);
            }
            try {
                int toMark = Integer.parseInt(parameters.get("argument")) - 1;
                tasks.markAsDone(toMark);
                return String.format(strings.getString("output.done"), tasks.get(toMark)).strip();
            } catch (NumberFormatException e) {
                throw new DukeInvalidParameterException(strings.getString("error.doneNum"), parameters);
            } catch (IndexOutOfBoundsException e) {
                throw new DukeInvalidParameterException(strings.getString("error.doneOut"), parameters);
            }
        }
    },
    TODO {
        @Override
        public String execute(HashMap<String, String> parameters, TaskList tasks) throws DukeException {
            if (parameters.isEmpty()) {
                throw new DukeInvalidParameterException(strings.getString("error.todo"), parameters);
            }
            if (parameters.size() > 1) {
                throw new DukeInvalidParameterException(strings.getString("error.todoExtra"), parameters);
            }

            Task toAdd = new Todo(parameters.get("argument"));
            tasks.add(toAdd);
            return String.format(strings.getString("output.added"), toAdd, tasks.size());
        }
    },
    DEADLINE {
        @Override
        public String execute(HashMap<String, String> parameters, TaskList tasks) throws DukeException {
            if (parameters.isEmpty() || !parameters.containsKey(strings.getString("parameter.by"))) {
                throw new DukeInvalidParameterException(strings.getString("error.deadline"), parameters);
            }

            try {
                LocalDate deadline = LocalDate.parse(parameters.get(strings.getString("parameter.by")));

                Task toAdd = new Deadline(parameters.get("argument"), deadline);
                tasks.add(toAdd);
                return String.format(strings.getString("output.added"), toAdd, tasks.size());

            } catch (DateTimeParseException e) {
                throw new DukeInvalidParameterException(strings.getString("error.dateFormat"), parameters);
            }
        }
    },
    EVENT {
        @Override
        public String execute(HashMap<String, String> parameters, TaskList tasks) throws DukeException {
            if (parameters.isEmpty() || !parameters.containsKey(strings.getString("parameter.at"))) {
                throw new DukeInvalidParameterException(strings.getString("error.event"), parameters);
            }

            try {
                LocalDate eventDate = LocalDate.parse(parameters.get(strings.getString("parameter.at")));

                Task toAdd = new Event(parameters.get("argument"), eventDate);
                tasks.add(toAdd);
                return String.format(strings.getString("output.added"), toAdd, tasks.size());

            } catch (DateTimeParseException e) {
                throw new DukeInvalidParameterException(strings.getString("error.dateFormat"), parameters);
            }
        }
    },
    DELETE {
        @Override
        public String execute(HashMap<String, String> parameters, TaskList tasks) throws DukeException {
            if (!parameters.containsKey("argument")) {
                throw new DukeInvalidParameterException(strings.getString("error.delete"), parameters);
            }
            try {
                int toDelete = Integer.parseInt(parameters.get("argument")) - 1;
                return String.format(strings.getString("output.delete"), tasks.remove(toDelete), tasks.size()).strip();
            } catch (NumberFormatException e) {
                throw new DukeInvalidParameterException(strings.getString("error.deleteNum"), parameters);
            } catch (IndexOutOfBoundsException e) {
                throw new DukeInvalidParameterException(strings.getString("error.deleteOut"), parameters);
            }
        }
    },
    FIND {
        @Override
        public String execute(HashMap<String, String> parameters, TaskList tasks) throws DukeException {
            if (!parameters.containsKey("argument")) {
                throw new DukeInvalidParameterException(strings.getString("error.find"), parameters);
            }
            String searchTerm = parameters.get("argument");
            List<Task> matches = tasks
                    .getList()
                    .stream()
                    .filter(item -> item.getDescription().contains(searchTerm))
                    .collect(Collectors.toList());

            String output = IntStream.range(0, matches.size())
                    .mapToObj(index -> (index + 1) + ". " + matches.get(index) + "\t\t")
                    .reduce("", (x, y) -> x + y)
                    .strip();
            if (output.equals("")) {
                return strings.getString("output.findEmpty");
            } else {
                return String.format(strings.getString("output.find"), output);
            }
        }
    },
    BYE {
        @Override
        public String execute(HashMap<String, String> parameters, TaskList tasks) {
            return strings.getString("output.bye");
        }
    };

    private static final ResourceBundle strings = ResourceBundle.getBundle("StringsBundle", Locale.ENGLISH);

    /**
     * Factory method to choose the appropriate <code>Command</code> from a given <code>String</code>
     *
     * @param command the command as a <code>String</code>
     * @return the command as a <code>Command</code>
     * @throws DukeUnrecognisedCommandException thrown when command is invalid
     */
    public static Command createCommand(String command) throws DukeUnrecognisedCommandException {
        Command ret;
        if (command.equals(strings.getString("command.list"))) {
            ret = LIST;
        } else if (command.equals(strings.getString("command.done"))) {
            ret = DONE;
        } else if (command.equals(strings.getString("command.todo"))) {
            ret = TODO;
        } else if (command.equals(strings.getString("command.deadline"))) {
            ret = DEADLINE;
        } else if (command.equals(strings.getString("command.event"))) {
            ret = EVENT;
        } else if (command.equals(strings.getString("command.delete"))) {
            ret = DELETE;
        } else if (command.equals(strings.getString("command.find"))) {
            ret = FIND;
        } else if (command.equals(strings.getString("command.bye"))) {
            ret = BYE;
        } else {
            throw new DukeUnrecognisedCommandException("Cannot Recognise Command ", command);
        }

        return ret;
    }

    /**
     * Executes the logic of the corresponding command.
     *
     * @param parameters the parameters
     * @param tasks      the tasks list for manipulation
     * @return the output of the command as a <code>String</code>
     * @throws DukeException general parent exception
     */
    public abstract String execute(HashMap<String, String> parameters, TaskList tasks) throws DukeException;
}
