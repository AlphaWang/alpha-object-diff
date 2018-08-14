package com.alphawang.diff;

import com.alphawang.diff.format.DiffResultFormatter;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Date;
import java.util.List;
import java.util.Map;

public class DiffResultFormatterTest {
    
    private DiffResult result;
    private Date date;
    
    @Before
    public void setup() {
        date = new Date();
        result = new DiffResult(ImmutableMap.of("id", 123), null, null);
    }
    
    @Test
    public void testCollectionSize() {
        List<MockItem> items1 = ImmutableList.of(
            MockItem.builder().itemId(1L).name("name1").modifiedAt(date).build(),
            MockItem.builder().itemId(2L).name("name2").modifiedAt(date).build()
        );

        List<MockItem> items2 = ImmutableList.of(
            MockItem.builder().itemId(1L).name("name1").modifiedAt(date).build(),
            MockItem.builder().itemId(2L).name("name2").modifiedAt(date).build(),
            MockItem.builder().itemId(3L).name("name3").modifiedAt(date).build()
        );
        
        result.add(Difference.of("path", Difference.DifferenceType.SIZE_NOT_SAME, items1, items2));

        String diff = DiffResultFormatter.format(result, null);

        Assert.assertNotNull(diff);
        Assert.assertTrue("id = 123\n" + "[path] SIZE_NOT_SAME - \n", diff.contains("id = 123\n" + "[path] SIZE_NOT_SAME - \n [L] [MockItem(itemId=1"));
    }

    @Test
    public void testCollectionSize_withFunction() {
        List<MockItem> items1 = ImmutableList.of(
            MockItem.builder().itemId(1L).name("name1").modifiedAt(date).build(),
            MockItem.builder().itemId(2L).name("name2").modifiedAt(date).build()
        );

        List<MockItem> items2 = ImmutableList.of(
            MockItem.builder().itemId(1L).name("name1").modifiedAt(date).build(),
            MockItem.builder().itemId(2L).name("name2").modifiedAt(date).build(),
            MockItem.builder().itemId(3L).name("name3").modifiedAt(date).build()
        );

        result.add(Difference.of("path", Difference.DifferenceType.SIZE_NOT_SAME, items1, items2));

        String diff = DiffResultFormatter.format(result, ImmutableMap.of(MockItem.class, (Function<MockItem, String>) input -> { return "itemId=" + input.getItemId(); }));

        Assert.assertNotNull(diff);
        Assert.assertEquals("id = 123\n" + "[path] SIZE_NOT_SAME - \n" + " [L] [itemId=1, itemId=2]\n" + " [R] [itemId=1, itemId=2, itemId=3]\n", diff);
    }

    @Test
    public void testMapSize_withFunction() {
        Map<Long, MockItem> items1 = ImmutableMap.of(
            1L, MockItem.builder().itemId(1L).name("name1").modifiedAt(date).build(),
            2L, MockItem.builder().itemId(2L).name("name2").modifiedAt(date).build()
        );

        Map<Long, MockItem> items2 = ImmutableMap.of(
            1L, MockItem.builder().itemId(1L).name("name1").modifiedAt(date).build(),
            2L, MockItem.builder().itemId(2L).name("name2").modifiedAt(date).build(),
            3L, MockItem.builder().itemId(3L).name("name3").modifiedAt(date).build()
        );

        result.add(Difference.of("path", Difference.DifferenceType.SIZE_NOT_SAME, items1, items2));

        String diff = DiffResultFormatter.format(result, ImmutableMap.of(MockItem.class, (Function<MockItem, String>) input -> { return "itemId=" + input.getItemId(); }));

        Assert.assertNotNull(diff);
        Assert.assertEquals("id = 123\n" + "[path] SIZE_NOT_SAME - \n" + " [L] {1=itemId=1, 2=itemId=2}\n" + " [R] {1=itemId=1, 2=itemId=2, 3=itemId=3}\n", diff);
    }
}
