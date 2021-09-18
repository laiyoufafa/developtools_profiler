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

import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.Image;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * test ImageUtils class .
 *
 * @date 2021/4/24 17:53
 */
class ImageUtilsTest {
    /**
     * test function the getStarFill .
     */
    @Test
    void getStarFill() {
        try {
            Image image = ImageIO.read(ImageUtils.class.getResourceAsStream("/assets/top_star_fill.png"));
            ImageUtils.getInstance().setStarFill(image);
        } catch (IOException e) {
            e.printStackTrace();
        }
        assertEquals(true, ImageUtils.getInstance().getStarFill() != null);
    }

    /**
     * test function the setStarFill .
     */
    @Test
    void setStarFill() {
        try {
            Image image = ImageIO.read(ImageUtils.class.getResourceAsStream("/assets/top_star_fill.png"));
            ImageUtils.getInstance().setStarFill(image);
        } catch (IOException e) {
            e.printStackTrace();
        }
        assertEquals(true, ImageUtils.getInstance().getStarFill() != null);
    }

    /**
     * test function the getStar .
     */
    @Test
    void getStar() {
        try {
            Image image = ImageIO.read(ImageUtils.class.getResourceAsStream("/assets/top_star_fill.png"));
            ImageUtils.getInstance().setStar(image);
        } catch (IOException e) {
            e.printStackTrace();
        }
        assertEquals(true, ImageUtils.getInstance().getStar() != null);
    }

    /**
     * test function the setStar .
     */
    @Test
    void setStar() {
        try {
            Image image = ImageIO.read(ImageUtils.class.getResourceAsStream("/assets/top_star_fill.png"));
            ImageUtils.getInstance().setStar(image);
        } catch (IOException e) {
            e.printStackTrace();
        }
        assertEquals(true, ImageUtils.getInstance().getStar() != null);
    }

    /**
     * test function the getCheckYes .
     */
    @Test
    void getCheckYes() {
        try {
            Image image = ImageIO.read(ImageUtils.class.getResourceAsStream("/assets/top_star_fill.png"));
            ImageUtils.getInstance().setCheckYes(image);
        } catch (IOException e) {
            e.printStackTrace();
        }
        assertEquals(true, ImageUtils.getInstance().getCheckYes() != null);
    }

    /**
     * test function the setCheckYes .
     */
    @Test
    void setCheckYes() {
        try {
            Image image = ImageIO.read(ImageUtils.class.getResourceAsStream("/assets/top_star_fill.png"));
            ImageUtils.getInstance().setCheckYes(image);
        } catch (IOException e) {
            e.printStackTrace();
        }
        assertEquals(true, ImageUtils.getInstance().getCheckYes() != null);
    }

    /**
     * test function the getCheckNo .
     */
    @Test
    void getCheckNo() {
        try {
            Image image = ImageIO.read(ImageUtils.class.getResourceAsStream("/assets/top_star_fill.png"));
            ImageUtils.getInstance().setCheckNo(image);
        } catch (IOException e) {
            e.printStackTrace();
        }
        assertEquals(true, ImageUtils.getInstance().getCheckNo() != null);
    }

    /**
     * test function the setCheckNo .
     */
    @Test
    void setCheckNo() {
        try {
            Image image = ImageIO.read(ImageUtils.class.getResourceAsStream("/assets/top_star_fill.png"));
            ImageUtils.getInstance().setCheckNo(image);
        } catch (IOException e) {
            e.printStackTrace();
        }
        assertEquals(true, ImageUtils.getInstance().getCheckNo() != null);
    }

    /**
     * test function the getArrowDown .
     */
    @Test
    void getArrowDown() {
        try {
            Image image = ImageIO.read(ImageUtils.class.getResourceAsStream("/assets/top_star_fill.png"));
            ImageUtils.getInstance().setArrowDown(image);
        } catch (IOException e) {
            e.printStackTrace();
        }
        assertEquals(true, ImageUtils.getInstance().getArrowDown() != null);
    }

    /**
     * test function the setArrowDown .
     */
    @Test
    void setArrowDown() {
        try {
            Image image = ImageIO.read(ImageUtils.class.getResourceAsStream("/assets/top_star_fill.png"));
            ImageUtils.getInstance().setArrowDown(image);
        } catch (IOException e) {
            e.printStackTrace();
        }
        assertEquals(true, ImageUtils.getInstance().getArrowDown() != null);
    }

    /**
     * test function the getArrowDownFocus .
     */
    @Test
    void getArrowDownFocus() {
        try {
            Image image = ImageIO.read(ImageUtils.class.getResourceAsStream("/assets/top_star_fill.png"));
            ImageUtils.getInstance().setArrowDownFocus(image);
        } catch (IOException e) {
            e.printStackTrace();
        }
        assertEquals(true, ImageUtils.getInstance().getArrowDownFocus() != null);
    }

    /**
     * test function the setArrowDownFocus .
     */
    @Test
    void setArrowDownFocus() {
        try {
            Image image = ImageIO.read(ImageUtils.class.getResourceAsStream("/assets/top_star_fill.png"));
            ImageUtils.getInstance().setArrowDownFocus(image);
        } catch (IOException e) {
            e.printStackTrace();
        }
        assertEquals(true, ImageUtils.getInstance().getArrowDownFocus() != null);
    }

    /**
     * test function the getArrowUp .
     */
    @Test
    void getArrowUp() {
        try {
            Image image = ImageIO.read(ImageUtils.class.getResourceAsStream("/assets/top_star_fill.png"));
            ImageUtils.getInstance().setArrowUpFocus(image);
        } catch (IOException e) {
            e.printStackTrace();
        }
        assertEquals(true, ImageUtils.getInstance().getArrowUpFocus() != null);
    }

    /**
     * test function the setArrowUp .
     */
    @Test
    void setArrowUp() {
        try {
            Image image = ImageIO.read(ImageUtils.class.getResourceAsStream("/assets/top_star_fill.png"));
            ImageUtils.getInstance().setArrowUpFocus(image);
        } catch (IOException e) {
            e.printStackTrace();
        }
        assertEquals(true, ImageUtils.getInstance().getArrowUpFocus() != null);
    }

    /**
     * test function the getArrowUpFocus .
     */
    @Test
    void getArrowUpFocus() {
        try {
            Image image = ImageIO.read(ImageUtils.class.getResourceAsStream("/assets/top_star_fill.png"));
            ImageUtils.getInstance().setArrowUpFocus(image);
        } catch (IOException e) {
            e.printStackTrace();
        }
        assertEquals(true, ImageUtils.getInstance().getArrowUpFocus() != null);
    }

    /**
     * test function the setArrowUpFocus .
     */
    @Test
    void setArrowUpFocus() {
        try {
            Image image = ImageIO.read(ImageUtils.class.getResourceAsStream("/assets/top_star_fill.png"));
            ImageUtils.getInstance().setArrowUpFocus(image);
        } catch (IOException e) {
            e.printStackTrace();
        }
        assertEquals(true, ImageUtils.getInstance().getArrowUpFocus() != null);
    }

    /**
     * test function the getInstance .
     */
    @Test
    void getInstance() {
        assertEquals(true, ImageUtils.getInstance() != null);
    }
}