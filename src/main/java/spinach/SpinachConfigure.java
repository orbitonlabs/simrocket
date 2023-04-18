package spinach;

import java.util.Map;
import java.util.TreeMap;
import java.util.Vector;

public class SpinachConfigure {

    private Vector<SpinachRule> rules;

    private String delimiter;

    public SpinachConfigure() {
        rules = new Vector<>(1,1);
    }

    public void expect(String key, String type) {
        rules.addElement(new SpinachRule(key, type));
    }

    public void setDelimiter(String d) {
        delimiter = d;
    }

    public Map<String, Object> evaluate(Vector<String> data) {
        Map<String, Object> map = new TreeMap<>();
        for(String st:data) {
            String[] d = st.split(delimiter);
            if(d.length != 2) {
                System.out.println("Spinach Error: Invalid configuration line \""+st+"\".");
                return null;
            }
            String k = d[0], v = d[1];
            for(SpinachRule rs:rules) if(k.compareTo(rs.key) == 0) {
                Object match = rs.match(v);
                if(match == null) {
                    System.out.println("Spinach Warning: Type Mismatch for\""+k+"\", mapping ignored.");
                    continue;
                }
                map.put(k, match);
            }
        }
        return map;
    }

}
