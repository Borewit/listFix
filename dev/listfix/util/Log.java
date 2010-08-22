
package listfix.util;

public class Log
{
    static public void write(String fmt, Object ... args)
    {
        System.out.print(_indentTxt);
        System.out.printf(fmt, args);
        System.out.println();
    }
    static private String _indentTxt = "";
    static private int _indentLevel;

    static public void indent()
    {
        _indentLevel++;
        refreshIndent();
    }

    static public void unindent()
    {
        _indentLevel--;
        refreshIndent();
    }

    static private void refreshIndent()
    {
        if (_indentLevel > 0)
        {
            StringBuilder sb = new StringBuilder();
            for (int ix = 0; ix < _indentLevel; ix++)
                sb.append("    ");
            _indentTxt = sb.toString();
        }
        else
            _indentTxt = "";
    }

}
