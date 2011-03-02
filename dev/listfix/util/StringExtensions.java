/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package listfix.util;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author jcaron
 */
public class StringExtensions
{
	public static List<String> splitCamelCase(String inputString)
	{
		List<String> result = new ArrayList<String>();
		char[] chars = inputString.toCharArray();
		StringBuilder buffer = new StringBuilder();
		for (int i = 0; i < chars.length; i++)
		{
			if (i == 0)
			{
				buffer.append(chars[i]);
			}
			else
			{
				if (Character.isUpperCase(chars[i]))
				{
					// start a new token in the buffer, saving the current buffer off to the result list
					result.add(buffer.toString());
					buffer.setLength(0);
					buffer.append(chars[i]);
				}
				else
				{
					buffer.append(chars[i]);
				}
			}
		}

		// buffer contains the last token, need to keep it.
		result.add(buffer.toString());
		buffer.setLength(0);
		return result;
	}
}
