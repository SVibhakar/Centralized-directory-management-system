//Name : Sejal Vibhakar
//ID : 1001765264
import java.io.Serializable;
import java.util.List;

// Class to represent commands sent between server and client.
class Command implements Serializable {

    private static final long serialVersionUID = -4137513186907998960L;
    private String command;
    private String args;
    private List<String> results;

    public Command(String command, String args) {
        setCommand(command);
        setArgs(args);
    }

    public List<String> getResults() {
        return results;
    }

    public void setResults(List<String> results) {
        this.results = results;
    }

    public String getCommand() {
        return command;
    }

    public String getArgs() {
        return args;
    }

    public void setArgs(String args) {
        this.args = args;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    @Override
    public String toString() {
        return this.command + " (" + args + ")";
    }

}
