package com.doskapps.interradio.callbacks;

import com.doskapps.interradio.models.Category;
import com.doskapps.interradio.models.Radio;

import java.util.ArrayList;
import java.util.List;

public class CallbackCategoryDetails {

    public String status = "";
    public int count = -1;
    public int count_total = -1;
    public int pages = -1;
    public Category category = null;
    public List<Radio> posts = new ArrayList<>();

}
