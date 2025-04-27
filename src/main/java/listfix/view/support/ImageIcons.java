package listfix.view.support;

import javax.swing.*;

public class ImageIcons {
  public static final ImageIcon IMG_MISSING =
      new ImageIcon(
          ImageIcons.class.getResource("/images/missing_text_icon.png")); // icon-missing.png"));
  public static final ImageIcon IMG_FOUND =
      new ImageIcon(
          ImageIcons.class.getResource("/images/found_text_icon.png")); // icon-found.png"));
  public static final ImageIcon IMG_FIXED =
      new ImageIcon(
          ImageIcons.class.getResource("/images/fixed_text_icon.png")); // icon-fixed.png"));
  public static final ImageIcon IMG_URL =
      new ImageIcon(ImageIcons.class.getResource("/images/url_text_icon.png")); // icon-url.png"));
}
