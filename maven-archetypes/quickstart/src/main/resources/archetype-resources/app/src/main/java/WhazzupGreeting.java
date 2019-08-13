package ${package};

import org.pf4j.Extension;

@Extension
public class WhazzupGreeting implements Greeting {

    @Override
    public String getGreeting() {
        return "Whazzup";
    }

}
