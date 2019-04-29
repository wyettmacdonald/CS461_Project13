/*
 * File: Style.java
 * Names: Wyett MacDonald, Tia Zhang
 * Project 15
 * Date: March 22, 2019
 */

package proj18DouglasMacDonaldZhang;

import org.fxmisc.richtext.model.StyleSpans;
import java.util.Collection;

/**
 * Interface Style that impelements computeHighlithing for JavaStyle and MIPSStyle
 */
public interface Style {

    StyleSpans<Collection<String>> computeHighlighting(String text);
}
