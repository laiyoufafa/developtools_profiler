/*
 * Copyright (c) 2021 Huawei Device Co., Ltd.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ohos.devtools.views.trace.util;

import javax.imageio.ImageIO;
import java.awt.Image;
import java.io.IOException;

/**
 * Image processing tools
 *
 * @version 1.0
 * @date 2021/04/22 12:25
 */
public final class ImageUtils {
    private static ImageUtils instance;

    private Image starFill;

    private Image star;

    private Image checkYes;

    private Image checkNo;

    private Image arrowDown;

    private Image arrowDownFocus;

    private Image arrowUp;

    private Image arrowUpFocus;

    private Image iconUsb;

    private Image iconWifi;

    private ImageUtils() {
        try {
            starFill = ImageIO.read(ImageUtils.class.getResourceAsStream("/assets/top_star_fill.png"));
            star = ImageIO.read(ImageUtils.class.getResourceAsStream("/assets/top_star.png"));
            checkNo = ImageIO.read(ImageUtils.class.getResourceAsStream("/assets/check_box_1.png"));
            checkYes = ImageIO.read(ImageUtils.class.getResourceAsStream("/assets/check_box_2.png"));
            arrowUp = ImageIO.read(ImageUtils.class.getResourceAsStream("/assets/arrow-up.png"));
            arrowUpFocus = ImageIO.read(ImageUtils.class.getResourceAsStream("/assets/arrow-up-2.png"));
            arrowDown = ImageIO.read(ImageUtils.class.getResourceAsStream("/assets/arrow-down.png"));
            arrowDownFocus = ImageIO.read(ImageUtils.class.getResourceAsStream("/assets/arrow-down-2.png"));
            iconUsb = ImageIO.read(ImageUtils.class.getResourceAsStream("/assets/icon_usb2.png"));
            iconWifi = ImageIO.read(ImageUtils.class.getResourceAsStream("/assets/icon_wifi2.png"));
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    /**
     * Get the current ImageUtils instantiation object
     *
     * @return ImageUtils
     */
    public static ImageUtils getInstance() {
        if (instance == null) {
            instance = new ImageUtils();
        }
        return instance;
    }

    /**
     * Gets the value of starFill .
     *
     * @return the value of java.awt.Image
     */
    public Image getStarFill() {
        return starFill;
    }

    /**
     * Sets the starFill .
     * <p>You can use getStarFill() to get the value of starFill</p>
     *
     * @param image image
     */
    public void setStarFill(final Image image) {
        this.starFill = image;
    }

    /**
     * Gets the value of star .
     *
     * @return the value of java.awt.Image
     */
    public Image getStar() {
        return star;
    }

    /**
     * Sets the star .
     * <p>You can use getStar() to get the value of star</p>
     *
     * @param image image
     */
    public void setStar(final Image image) {
        this.star = image;
    }

    /**
     * Gets the value of checkYes .
     *
     * @return the value of java.awt.Image
     */
    public Image getCheckYes() {
        return checkYes;
    }

    /**
     * Sets the checkYes .
     * <p>You can use getCheckYes() to get the value of checkYes</p>
     *
     * @param image image
     */
    public void setCheckYes(final Image image) {
        this.checkYes = image;
    }

    /**
     * Gets the value of checkNo .
     *
     * @return the value of java.awt.Image
     */
    public Image getCheckNo() {
        return checkNo;
    }

    /**
     * Sets the checkNo .
     * <p>You can use getCheckNo() to get the value of checkNo</p>
     *
     * @param image image
     */
    public void setCheckNo(final Image image) {
        this.checkNo = image;
    }

    /**
     * Gets the value of arrowDown .
     *
     * @return the value of java.awt.Image
     */
    public Image getArrowDown() {
        return arrowDown;
    }

    /**
     * Sets the arrowDown .
     * <p>You can use getArrowDown() to get the value of arrowDown</p>
     *
     * @param image image
     */
    public void setArrowDown(final Image image) {
        this.arrowDown = image;
    }

    /**
     * Gets the value of arrowDownFocus .
     *
     * @return the value of java.awt.Image
     */
    public Image getArrowDownFocus() {
        return arrowDownFocus;
    }

    /**
     * Sets the arrowDownFocus .
     * <p>You can use getArrowDownFocus() to get the value of arrowDownFocus</p>
     *
     * @param image image
     */
    public void setArrowDownFocus(final Image image) {
        this.arrowDownFocus = image;
    }

    /**
     * Gets the value of arrowUp .
     *
     * @return the value of java.awt.Image
     */
    public Image getArrowUp() {
        return arrowUp;
    }

    /**
     * Sets the arrowUp .
     * <p>You can use getArrowUp() to get the value of arrowUp</p>
     *
     * @param image image
     */
    public void setArrowUp(final Image image) {
        this.arrowUp = image;
    }

    /**
     * Gets the value of arrowUpFocus .
     *
     * @return the value of java.awt.Image
     */
    public Image getArrowUpFocus() {
        return arrowUpFocus;
    }

    /**
     * Sets the arrowUpFocus .
     * <p>You can use getArrowUpFocus() to get the value of arrowUpFocus</p>
     *
     * @param image image
     */
    public void setArrowUpFocus(final Image image) {
        arrowUpFocus = image;
    }

    public Image getIconUsb() {
        return iconUsb;
    }

    public Image getIconWifi() {
        return iconWifi;
    }
}
