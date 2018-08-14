package com.alphawang.diff;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class ObjectDiffTest {

    private Map<String, String> identities;
    private MockItems leftItems;
    private MockItems rightItems;

    private Date date;
    private LocalDate localDate;


    @Before
    public void setup() {
        date = new Date();
        localDate = LocalDate.now();

        List<MockItem> items1 = ImmutableList.of(
            MockItem.builder().itemId(1L).modifiedAt(date).createdAt(localDate).build(),
            MockItem.builder().itemId(2L).modifiedAt(date).createdAt(localDate).build()
        );

        Map<Long, MockItem> itemMap1 = ImmutableMap.of(
            3L, MockItem.builder().itemId(3L).modifiedAt(date).createdAt(localDate).build(),
            4L, MockItem.builder().itemId(4L).modifiedAt(date).createdAt(localDate).build()
        );


        leftItems = MockItems.builder()
            .memberSrl("AAA")
            .id(1000L)
            .itemList(Lists.newArrayList(items1))
            .itemMap(Maps.newHashMap(itemMap1))
            .build();

        List<MockItem> items2 = ImmutableList.of(
            MockItem.builder().itemId(1L).modifiedAt(date).createdAt(localDate).build(),
            MockItem.builder().itemId(2L).modifiedAt(date).createdAt(localDate).build()
        );

        Map<Long, MockItem> itemMap2 = ImmutableMap.of(
            3L, MockItem.builder().itemId(3L).modifiedAt(date).createdAt(localDate).build(),
            4L, MockItem.builder().itemId(4L).modifiedAt(date).createdAt(localDate).build()
        );
        rightItems = MockItems.builder()
            .memberSrl("AAA")
            .id(1000L)
            .itemList(Lists.newArrayList(items2))
            .itemMap(Maps.newHashMap(itemMap2))
            .build();

        identities = ImmutableMap.of("memberSrl", "AAA");
    }

    @Test
    public void testNullVsNull() {
        DiffResult diffResult = ObjectDiff.newInstance().withIdentities(identities).withLeft(null).withRight(null).diff();
        Assert.assertNotNull(diffResult);
        Assert.assertFalse(diffResult.hasDifference());
        Assert.assertTrue(diffResult.getDifferences().isEmpty());
    }

    @Test
    public void testEmptyVsNull() {
        DiffResult diffResult = ObjectDiff.newInstance().withIdentities(identities).withLeft(MockItems.builder().build()).withRight(null).diff();
        Assert.assertNotNull(diffResult);
        Assert.assertTrue(diffResult.hasDifference());
        Assert.assertEquals(1, diffResult.getDifferences().size());
    }

    @Test
    public void testSame() {
        DiffResult diffResult = ObjectDiff.newInstance().withIdentities(identities).withLeft(leftItems).withRight(rightItems).diff();
        Assert.assertNotNull(diffResult);
        Assert.assertFalse(diffResult.hasDifference());
        Assert.assertTrue(diffResult.getDifferences().isEmpty());
    }

    @Test
    public void testDiffLong() {
        rightItems.setId(2000L);
        String diffPath = "/id";

        DiffResult diffResult = ObjectDiff.newInstance().withIdentities(identities).withLeft(leftItems).withRight(rightItems).diff();
        Assert.assertNotNull(diffResult);
        Assert.assertTrue(diffResult.hasDifference());

        Map<String, Difference> differences = diffResult.getDifferences();
        Assert.assertTrue(differences.size() == 1);
        Assert.assertTrue(differences.containsKey(diffPath));

        Difference difference = differences.get(diffPath);
        Assert.assertEquals(diffPath, difference.getPath());
        Assert.assertEquals(1000L, difference.getLeftValue());
        Assert.assertEquals(2000L, difference.getRightValue());
    }

    @Test
    public void testDiffString() {
        rightItems.setMemberSrl("BBB");
        String diffPath = "/memberSrl";

        DiffResult diffResult = ObjectDiff.newInstance().withIdentities(identities).withLeft(leftItems).withRight(rightItems).diff();
        Assert.assertNotNull(diffResult);
        Assert.assertTrue(diffResult.hasDifference());

        Map<String, Difference> differences = diffResult.getDifferences();
        Assert.assertTrue(differences.size() == 1);
        Assert.assertTrue(differences.containsKey(diffPath));

        Difference difference = differences.get(diffPath);
        Assert.assertEquals(diffPath, difference.getPath());
        Assert.assertEquals("AAA", difference.getLeftValue());
        Assert.assertEquals("BBB", difference.getRightValue());
    }


    @Test
    public void testDiffCollectionLong() {
        rightItems.getItemList().get(0).setItemId(11L);
        rightItems.setId(2000L);

        String diffPath_id = "/id";
        String diffPath_collection = "/itemList/0/itemId";

        DiffResult diffResult = ObjectDiff.newInstance().withIdentities(identities).withLeft(leftItems).withRight(rightItems).diff();
        Assert.assertNotNull(diffResult);
        Assert.assertTrue(diffResult.hasDifference());

        Map<String, Difference> differences = diffResult.getDifferences();
        Assert.assertTrue(differences.size() == 2);
        Assert.assertTrue(differences.containsKey(diffPath_id));
        Assert.assertTrue(differences.containsKey(diffPath_collection));

        Difference difference = differences.get(diffPath_collection);
        Assert.assertEquals(diffPath_collection, difference.getPath());
        Assert.assertEquals(1L, difference.getLeftValue());
        Assert.assertEquals(11L, difference.getRightValue());
    }

    @Test
    public void testDiffCollectionLongWithKeyFunction() {
        rightItems.getItemList().get(0).setItemId(11L);

        String diffPath_collection = "/itemList/1";

        DiffResult diffResult = ObjectDiff.newInstance()
            .withIdentities(identities)
            .withLeft(leftItems)
            .withRight(rightItems)
            .addCollectionItemKeyFunction(MockItem.class, (Function<MockItem, Long>) input -> input.getItemId())
            .diff();
        Assert.assertNotNull(diffResult);
        Assert.assertTrue(diffResult.hasDifference());

        Map<String, Difference> differences = diffResult.getDifferences();
        Assert.assertTrue(differences.size() == 1);
        Assert.assertTrue(differences.containsKey(diffPath_collection));

        Difference difference = differences.get(diffPath_collection);
        Assert.assertEquals(diffPath_collection, difference.getPath());
        Assert.assertEquals(Difference.DifferenceType.NULL_VS_NONNULL, difference.getType());
        Assert.assertNotNull(difference.getLeftValue());
        Assert.assertNull( difference.getRightValue());
    }

    @Test
    public void testDiffCollectionOrder() {
        List<MockItem> items2 = ImmutableList.of(
            MockItem.builder().itemId(2L).modifiedAt(date).createdAt(localDate).build(),
            MockItem.builder().itemId(1L).modifiedAt(date).createdAt(localDate).build()

        );
        rightItems.setItemList(items2);

        String diffPath_collection1 = "/itemList/0/itemId";
        String diffPath_collection2 = "/itemList/1/itemId";

        DiffResult diffResult = ObjectDiff.newInstance().withIdentities(identities).withLeft(leftItems).withRight(rightItems).diff();
        Assert.assertNotNull(diffResult);
        Assert.assertTrue(diffResult.hasDifference());

        Map<String, Difference> differences = diffResult.getDifferences();
        Assert.assertTrue(differences.size() == 2);
        Assert.assertTrue(differences.containsKey(diffPath_collection1));
        Assert.assertTrue(differences.containsKey(diffPath_collection2));

        Difference difference = differences.get(diffPath_collection1);
        Assert.assertEquals(diffPath_collection1, difference.getPath());
        Assert.assertEquals(1L, difference.getLeftValue());
        Assert.assertEquals(2L, difference.getRightValue());
    }

    @Test
    public void testDiffCollectionOrderWithKey() {
        List<MockItem> items2 = ImmutableList.of(
            MockItem.builder().itemId(2L).modifiedAt(date).createdAt(localDate).build(),
            MockItem.builder().itemId(1L).modifiedAt(date).createdAt(localDate).build()

        );
        rightItems.setItemList(items2);

        DiffResult diffResult = ObjectDiff.newInstance().withIdentities(identities).withLeft(leftItems).withRight(rightItems)
            .addCollectionItemKeyFunction(MockItem.class, (Function<MockItem, Long>) input -> input.getItemId())
            .diff();
        Assert.assertNotNull(diffResult);
        Assert.assertFalse(diffResult.hasDifference());
    }

    @Test
    public void testDiffCollectionOrderWithWrongKey() {
        List<MockItem> items2 = ImmutableList.of(
            MockItem.builder().itemId(2L).modifiedAt(date).createdAt(localDate).build(),
            MockItem.builder().itemId(1L).modifiedAt(date).createdAt(localDate).build()

        );
        rightItems.setItemList(items2);

        String diffPath_collection1 = "/itemList/0/itemId";
        String diffPath_collection2 = "/itemList/1/itemId";

        DiffResult diffResult = ObjectDiff.newInstance().withIdentities(identities).withLeft(leftItems).withRight(rightItems)
            .addCollectionItemKeyFunction(MockItem.class, (Function<MockItems, Long>) input -> input.getId())
            .diff();
        Assert.assertNotNull(diffResult);
        Assert.assertTrue(diffResult.hasDifference());

        Map<String, Difference> differences = diffResult.getDifferences();
        Assert.assertTrue(differences.size() == 2);
        Assert.assertTrue(differences.containsKey(diffPath_collection1));
        Assert.assertTrue(differences.containsKey(diffPath_collection2));

        Difference difference = differences.get(diffPath_collection1);
        Assert.assertEquals(diffPath_collection1, difference.getPath());
        Assert.assertEquals(1L, difference.getLeftValue());
        Assert.assertEquals(2L, difference.getRightValue());
    }

    @Test
    public void testDiffCollectionDate() {
        Date newDate = new Date();
        newDate.setYear(2000);
        rightItems.getItemList().get(0).setModifiedAt(newDate);
        rightItems.getItemList().get(0).setItemId(11L);

        String diffPath_collection1 = "/itemList/0/itemId";
        String diffPath_collection2 = "/itemList/0/modifiedAt";

        DiffResult diffResult = ObjectDiff.newInstance().withIdentities(identities).withLeft(leftItems).withRight(rightItems).diff();
        Assert.assertNotNull(diffResult);
        Assert.assertTrue(diffResult.hasDifference());

        Map<String, Difference> differences = diffResult.getDifferences();
        Assert.assertTrue(differences.size() == 2);
        Assert.assertTrue(differences.containsKey(diffPath_collection1));

        Difference difference = differences.get(diffPath_collection1);
        Assert.assertEquals(diffPath_collection1, difference.getPath());

        Difference difference2 = differences.get(diffPath_collection2);
        Assert.assertEquals(diffPath_collection2, difference2.getPath());
    }

    @Test
    public void testDiffIgnorePath() {
        Date newDate = new Date();
        newDate.setYear(2000);
        rightItems.getItemList().get(0).setModifiedAt(newDate);
        rightItems.getItemList().get(0).setItemId(11L);

        String diffPath_collection1 = "/itemList/0/itemId";
        String diffPath_collection2 = "/itemList/0/modifiedAt";

        DiffResult diffResult = ObjectDiff.newInstance()
            .withIdentities(identities)
            .withLeft(leftItems)
            .withRight(rightItems)
            .withIgnorePaths(Lists.newArrayList(diffPath_collection2))
            .diff();
        Assert.assertNotNull(diffResult);
        Assert.assertTrue(diffResult.hasDifference());

        Map<String, Difference> differences = diffResult.getDifferences();
        Assert.assertTrue(differences.size() == 1);
        Assert.assertTrue(differences.containsKey(diffPath_collection1));

        Difference difference = differences.get(diffPath_collection1);
        Assert.assertEquals(diffPath_collection1, difference.getPath());
    }

    @Test
    public void testDiffCollectionDateWithKeyFunction() {
        Date newDate = new Date();
        newDate.setYear(2000);
        rightItems.getItemList().get(0).setModifiedAt(newDate);

        String diffPath_collection = "/itemList/1/modifiedAt";  // 1 is itemId

        DiffResult diffResult = ObjectDiff.newInstance()
            .withIdentities(identities)
            .withLeft(leftItems)
            .withRight(rightItems)
            .addCollectionItemKeyFunction(MockItem.class, (Function<MockItem, Long>) input -> input.getItemId())
            .diff();
        Assert.assertNotNull(diffResult);
        Assert.assertTrue(diffResult.hasDifference());

        Map<String, Difference> differences = diffResult.getDifferences();
        Assert.assertTrue(differences.size() == 1);
        Assert.assertTrue(differences.containsKey(diffPath_collection));

        Difference difference = differences.get(diffPath_collection);
        Assert.assertEquals(diffPath_collection, difference.getPath());

    }

    @Test
    public void testDiffCollectionSize() {
        rightItems.getItemList().set(1, null);

        String diffPath_collection = "/itemList/1";

        DiffResult diffResult = ObjectDiff.newInstance().withIdentities(identities).withLeft(leftItems).withRight(rightItems).diff();
        Assert.assertNotNull(diffResult);
        Assert.assertTrue(diffResult.hasDifference());

        Map<String, Difference> differences = diffResult.getDifferences();
        Assert.assertTrue(differences.size() == 1);
        Assert.assertTrue(differences.containsKey(diffPath_collection));

        Difference difference = differences.get(diffPath_collection);
        Assert.assertEquals(diffPath_collection, difference.getPath());
        Assert.assertNotNull(difference.getLeftValue());
        Assert.assertNull(difference.getRightValue());

        MockItem left = (MockItem) difference.getLeftValue();
        Assert.assertTrue(left.getItemId().equals(2L));
    }


    @Test
    public void testDiffCollectionNull() {
        rightItems.setItemList(null);

        String diffPath_collection = "/itemList";

        DiffResult diffResult = ObjectDiff.newInstance().withIdentities(identities).withLeft(leftItems).withRight(rightItems).diff();
        Assert.assertNotNull(diffResult);
        Assert.assertTrue(diffResult.hasDifference());

        Map<String, Difference> differences = diffResult.getDifferences();
        Assert.assertTrue(differences.size() == 1);
        Assert.assertTrue(differences.containsKey(diffPath_collection));

        Difference difference = differences.get(diffPath_collection);
        Assert.assertEquals(diffPath_collection, difference.getPath());
        Assert.assertNotNull(difference.getLeftValue());
        Assert.assertNull(difference.getRightValue());
    }

    @Test
    public void testDiffCollectionEmpty() {
        rightItems.setItemList(Collections.emptyList());

        String diffPath_collection = "/itemList";

        DiffResult diffResult = ObjectDiff.newInstance().withIdentities(identities).withLeft(leftItems).withRight(rightItems).diff();
        Assert.assertNotNull(diffResult);
        Assert.assertTrue(diffResult.hasDifference());

        Map<String, Difference> differences = diffResult.getDifferences();
        Assert.assertTrue(differences.size() == 1);
        Assert.assertTrue(differences.containsKey(diffPath_collection));

        Difference difference = differences.get(diffPath_collection);
        Assert.assertEquals(diffPath_collection, difference.getPath());
        Assert.assertNotNull(difference.getLeftValue());
        Assert.assertNotNull(difference.getRightValue());
        Assert.assertEquals(0, ((List) difference.getRightValue()).size());
    }

    @Test
    public void testDiffArrayPrimitive() {
        leftItems.setIntArray(new int[]{1, 2});
        rightItems.setIntArray(new int[]{1, 4});

        String diffPath = "/intArray/1";

        DiffResult diffResult = ObjectDiff.newInstance().withIdentities(identities).withLeft(leftItems).withRight(rightItems).diff();
        Assert.assertNotNull(diffResult);
        Assert.assertTrue(diffResult.hasDifference());

        Map<String, Difference> differences = diffResult.getDifferences();
        Assert.assertTrue(differences.size() == 1);
        Assert.assertTrue(differences.containsKey(diffPath));

        Difference difference = differences.get(diffPath);
        Assert.assertEquals(diffPath, difference.getPath());
        Assert.assertEquals(2, difference.getLeftValue());
        Assert.assertEquals(4, difference.getRightValue());
    }
    @Test
    public void testDiffArrayPrimitive_size() {
        leftItems.setIntArray(new int[]{1, 2});
        rightItems.setIntArray(new int[]{1});

        String diffPath = "/intArray";

        DiffResult diffResult = ObjectDiff.newInstance().withIdentities(identities).withLeft(leftItems).withRight(rightItems).diff();
        Assert.assertNotNull(diffResult);
        Assert.assertTrue(diffResult.hasDifference());

        Map<String, Difference> differences = diffResult.getDifferences();
        Assert.assertTrue(differences.size() == 1);
        Assert.assertTrue(differences.containsKey(diffPath));

        Difference difference = differences.get(diffPath);
        Assert.assertEquals(diffPath, difference.getPath());
        Assert.assertEquals(Difference.DifferenceType.SIZE_NOT_SAME, difference.getType());
    }

    @Test
    public void testDiffArrayObject() {
        leftItems.setItemArray(new MockItem[] {
            MockItem.builder().itemId(1L).build(),
            MockItem.builder().itemId(2L).build()});
        rightItems.setItemArray(new MockItem[] {
            MockItem.builder().itemId(1L).build(),
            MockItem.builder().itemId(3L).build()});

        String diffPath = "/itemArray/1/itemId";

        DiffResult diffResult = ObjectDiff.newInstance().withIdentities(identities).withLeft(leftItems).withRight(rightItems).diff();
        Assert.assertNotNull(diffResult);
        Assert.assertTrue(diffResult.hasDifference());

        Map<String, Difference> differences = diffResult.getDifferences();
        Assert.assertTrue(differences.size() == 1);
        Assert.assertTrue(differences.containsKey(diffPath));

        Difference difference = differences.get(diffPath);
        Assert.assertEquals(diffPath, difference.getPath());
        Assert.assertEquals(2L, difference.getLeftValue());
        Assert.assertEquals(3L, difference.getRightValue());
    }

    @Test
    public void testDiffMap() {
        rightItems.getItemMap().get(4L).setItemId(11111L);

        String diffPath = "/itemMap/4/itemId";

        DiffResult diffResult = ObjectDiff.newInstance().withIdentities(identities).withLeft(leftItems).withRight(rightItems).diff();
        Assert.assertNotNull(diffResult);
        Assert.assertTrue(diffResult.hasDifference());

        Map<String, Difference> differences = diffResult.getDifferences();
        Assert.assertTrue(differences.size() == 1);
        Assert.assertTrue(differences.containsKey(diffPath));

        Difference difference = differences.get(diffPath);
        Assert.assertEquals(diffPath, difference.getPath());
        Assert.assertEquals(4L, difference.getLeftValue());
        Assert.assertEquals(11111L, difference.getRightValue());
    }

    @Test
    public void testRootArray() {
        DiffResult diffResult = ObjectDiff.newInstance().withLeft(new int[]{1, 2}).withRight(new int[]{1, 3}).diff();
        Assert.assertNotNull(diffResult);
        Assert.assertTrue(diffResult.hasDifference());

        String diffPath = "/1";
        Map<String, Difference> differences = diffResult.getDifferences();
        Assert.assertTrue(differences.size() == 1);
        Assert.assertTrue(differences.containsKey(diffPath));

        Difference difference = differences.get(diffPath);
        Assert.assertEquals(diffPath, difference.getPath());
        Assert.assertEquals(2, difference.getLeftValue());
        Assert.assertEquals(3, difference.getRightValue());
    }

    @Test
    public void testRootArray_empty() {
        DiffResult diffResult = ObjectDiff.newInstance().withLeft(new int[]{1, 2}).withRight(new int[]{}).diff();
        Assert.assertNotNull(diffResult);
        Assert.assertTrue(diffResult.hasDifference());

        String diffPath = "";
        Map<String, Difference> differences = diffResult.getDifferences();
        Assert.assertTrue(differences.size() == 1);
        Assert.assertTrue(differences.containsKey(diffPath));

        Difference difference = differences.get(diffPath);
        Assert.assertEquals(diffPath, difference.getPath());
        Assert.assertEquals(Difference.DifferenceType.SIZE_NOT_SAME, difference.getType());
    }
}