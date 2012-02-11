package org.jschema.util;

import junit.framework.TestCase;
import org.jschema.model.JsonMap;
import org.junit.Ignore;

import java.io.InputStreamReader;
import java.util.*;

@Ignore
public class JSchemaUtilsTest extends TestCase {

  public void testBasicNames() throws Exception {
    assertEquals("NiceName", JSchemaUtils.convertJSONStringToGosuIdentifier("nice_name"));
    assertEquals("NiceNameSecondName", JSchemaUtils.convertJSONStringToGosuIdentifier("nice_name_second_name"));
    assertEquals("niceName", JSchemaUtils.convertJSONStringToGosuIdentifier("nice_name", false));
  }

  public void testConvertingJsonToJSchema() throws Exception {
    String content = null;
    Scanner scan = null;
    try {
      InputStreamReader reader = new InputStreamReader(getClass().getResourceAsStream("/org/jschema/examples/RegularJson.json"));
      scan = new Scanner(reader);
      scan.useDelimiter("\\Z");
      content = scan.next();
    } finally {
      if (scan != null) { scan.close(); }
    }

    Map json = (Map) JSchemaUtils.parseJson(content);
    json = (Map)JSchemaUtils.convertJsonToJSchema(json);
    Map someType = (Map)json.get("some_type");
    Map nested = (Map)someType.get("nested_type");
    Map nestedListEntry = (Map)((List)someType.get("type_in_array")).get(0);

    assertEquals("int", someType.get("int_ex"));
    assertEquals("string", someType.get("string_ex"));
    assertEquals("boolean", someType.get("boolean_ex"));
    assertEquals("string", nestedListEntry.get("content"));
    assertEquals("string", nested.get("nested_string_ex"));
    assertEquals("string", ((Map)((List)nested.get("nested_type_in_array")).get(0)).get("value"));
    assertEquals("int", ((List) nested.get("int_array_ex")).get(0));
    assertEquals("string", ((List)nested.get("string_array_ex")).get(0));
    assertEquals("int", nested.get("nested_int_ex"));
    assertEquals("number", nested.get("nested_number_ex"));
  }

  public void testSerializeStrings() {
    assertEquals("\"blah\\\"blah\"", JSchemaUtils.serializeJson("blah\"blah"));
    assertEquals("\"blah\\\\blah\"", JSchemaUtils.serializeJson("blah\\blah"));
    assertEquals("\"blah\\bblah\"", JSchemaUtils.serializeJson("blah\bblah"));
    assertEquals("\"blah\\fblah\"", JSchemaUtils.serializeJson("blah\fblah"));
    assertEquals("\"blah\\nblah\"", JSchemaUtils.serializeJson("blah\nblah"));
    assertEquals("\"blah\\rblah\"", JSchemaUtils.serializeJson("blah\rblah"));
    assertEquals("\"blah\\tblah\"", JSchemaUtils.serializeJson("blah\tblah"));
    assertEquals("\"blah\\u1234blah\"", JSchemaUtils.serializeJson("blah\u1234blah"));
  }

  public void testSerialize() {
    Map m = new JsonMap();
    m.put("foo", 10);
    m.put("bar", Arrays.asList(1, 2, 3));
    m.put("empty_map", new JsonMap());
    JsonMap subMap = new JsonMap();
    m.put("map", subMap);
    subMap.put("empty_map", new JsonMap());
    JsonMap subMapMap = new JsonMap();
    subMap.put("map", subMapMap);
    subMapMap.put("foo", "bar");

    assertEquals("{\"foo\" : 10, \"bar\" : [1, 2, 3], \"empty_map\" : {}, \"map\" : {\"empty_map\" : {}, \"map\" : {\"foo\" : \"bar\"}}}",
      JSchemaUtils.serializeJson(m));

    assertEquals("{\n" +
      "  \"foo\" : 10, \n" +
      "  \"bar\" : [1, 2, 3], \n" +
      "  \"empty_map\" : {}, \n" +
      "  \"map\" : {\n" +
      "    \"empty_map\" : {}, \n" +
      "    \"map\" : {\n" +
      "      \"foo\" : \"bar\"\n" +
      "    }\n" +
      "  }\n" +
      "}",
      JSchemaUtils.serializeJson(m, 2));

    assertEquals("{\n" +
      "    \"foo\" : 10, \n" +
      "    \"bar\" : [1, 2, 3], \n" +
      "    \"empty_map\" : {}, \n" +
      "    \"map\" : {\n" +
      "        \"empty_map\" : {}, \n" +
      "        \"map\" : {\n" +
      "            \"foo\" : \"bar\"\n" +
      "        }\n" +
      "    }\n" +
      "}",
      JSchemaUtils.serializeJson(m, 4));


    HashMap map2 = new HashMap();
    map2.put("foo", "bar");
    List lst = Arrays.asList(Collections.EMPTY_MAP, Collections.EMPTY_MAP, map2);
    assertEquals("[{}, {}, {\"foo\" : \"bar\"}]", JSchemaUtils.serializeJson(lst));

    assertEquals("[{}, {}, \n" +
                 "  {\n" +
                 "    \"foo\" : \"bar\"\n" +
                 "  }\n" +
                 "]", JSchemaUtils.serializeJson(lst, 2));

    assertEquals("[[{}, {}, \n" +
                 "    {\n" +
                 "      \"foo\" : \"bar\"\n" +
                 "    }\n" +
                 "  ]]", JSchemaUtils.serializeJson(Arrays.asList(lst), 2));

    HashMap map3 = new HashMap();
    map3.put("foo", lst);

    assertEquals("{\n" +
                 "  \"foo\" : [{}, {}, \n" +
                 "    {\n" +
                 "      \"foo\" : \"bar\"\n" +
                 "    }\n" +
                 "  ]\n" +
                 "}", JSchemaUtils.serializeJson(map3, 2));
  }

  public void testLongSerialize() {
    Map map = new HashMap();
    map.put("int_key", 123123123123l);
    assertEquals("{\"int_key\" : 123123123123}", JSchemaUtils.serializeJson(map));
  }


  public void testDateSerializationWorks() {
    Calendar cal = new GregorianCalendar(1999, 12, 30, 0, 0, 0);
    cal.setTimeZone(TimeZone.getTimeZone("Z"));
    while (cal.before(new GregorianCalendar(2001, 1, 2))) {
      Date time = cal.getTime();
      Date roundTrip = JSchemaUtils.parseDate(JSchemaUtils.serializeDate(time));
      if (!time.equals(roundTrip)) {
        String str = JSchemaUtils.serializeDate(time);
        Date reparsedDate = JSchemaUtils.parseDate(str);
        fail("Found unequal dates!");
      }
      cal.add(Calendar.MINUTE, 1);
      cal.add(Calendar.SECOND, 1);
      cal.add(Calendar.MILLISECOND, 1);
    }
  }

  public void testDateInJSONDateAndURIConversions() {
    assertEquals("date", JSchemaUtils.convertJsonToJSchema("2011-08-19T22:46:50-07:00"));
    assertEquals("uri", JSchemaUtils.convertJsonToJSchema("http://www.google.com"));
  }

  public void testGitHubResultConvertsProperly() {
    JsonMap lst = (JsonMap) JSchemaUtils.convertJsonToJSchema(JSchemaUtils.parseJson("{\n" +
      "    \"commits\": [{\n" +
      "        \"parents\": [{\n" +
      "            \"id\": \"e33fad9e988f6014d9872c264e4f342941d9f6a6\"\n" +
      "        }],\n" +
      "        \"author\": {\n" +
      "            \"name\": \"Vicent Marti\",\n" +
      "            \"login\": \"tanoku\",\n" +
      "            \"email\": \"tanoku@gmail.com\"\n" +
      "        },\n" +
      "        \"url\": \"/mojombo/grit/commit/ff015074ef35bd94cba943f9c0f98e161ab5851c\",\n" +
      "        \"id\": \"ff015074ef35bd94cba943f9c0f98e161ab5851c\",\n" +
      "        \"committed_date\": \"2011-08-19T22:50:25-07:00\",\n" +
      "        \"authored_date\": \"2011-08-19T22:46:50-07:00\",\n" +
      "        \"message\": \"100% Git-compliant actor creation\\n\\nSome more tweaks here:\\n\\n\\t- Do not use `strftime`, because it's not assured\\n\\tto be cross-platform\\n\\n\\t- Use C-like string formatting for Great Glory\\n\\tWhen Printing Numbers.\\n\\n\\t- Always print an email address -- even if we don't\\n\\thave one. A missing email field will crash `fsck`.\",\n" +
      "        \"tree\": \"74aa881a22754b94c99135a4e19276f5fe9c3b47\",\n" +
      "        \"committer\": {\n" +
      "            \"name\": \"Vicent Marti\",\n" +
      "            \"login\": \"tanoku\",\n" +
      "            \"email\": \"tanoku@gmail.com\"\n" +
      "        }\n" +
      "    },\n" +
      "    {\n" +
      "        \"parents\": [{\n" +
      "            \"id\": \"a9dbc43a2f8b82ce7698fa118466177a2929e45e\"\n" +
      "        }],\n" +
      "        \"author\": {\n" +
      "            \"name\": \"Vicent Marti\",\n" +
      "            \"login\": \"tanoku\",\n" +
      "            \"email\": \"tanoku@gmail.com\"\n" +
      "        },\n" +
      "        \"url\": \"/mojombo/grit/commit/e33fad9e988f6014d9872c264e4f342941d9f6a6\",\n" +
      "        \"id\": \"e33fad9e988f6014d9872c264e4f342941d9f6a6\",\n" +
      "        \"committed_date\": \"2011-08-17T12:03:40-07:00\",\n" +
      "        \"authored_date\": \"2011-08-16T16:16:12-07:00\",\n" +
      "        \"message\": \"Properly print time offsets\",\n" +
      "        \"tree\": \"901605a5fd9e4e3a1e3ecdcee3ace0506cad86be\",\n" +
      "        \"committer\": {\n" +
      "            \"name\": \"Chris Wanstrath\",\n" +
      "            \"login\": \"defunkt\",\n" +
      "            \"email\": \"chris@ozmm.org\"\n" +
      "        }\n" +
      "    },\n" +
      "    {\n" +
      "        \"parents\": [{\n" +
      "            \"id\": \"568935ddda3168ddbe876163ace842eeb3d62e0a\"\n" +
      "        }],\n" +
      "        \"author\": {\n" +
      "            \"name\": \"Ryan Tomayko\",\n" +
      "            \"login\": \"rtomayko\",\n" +
      "            \"email\": \"rtomayko@gmail.com\"\n" +
      "        },\n" +
      "        \"url\": \"/mojombo/grit/commit/a9dbc43a2f8b82ce7698fa118466177a2929e45e\",\n" +
      "        \"id\": \"a9dbc43a2f8b82ce7698fa118466177a2929e45e\",\n" +
      "        \"committed_date\": \"2011-07-10T15:27:04-07:00\",\n" +
      "        \"authored_date\": \"2011-07-10T14:24:38-07:00\",\n" +
      "        \"message\": \"handle newlines in author / committer\\n\\nThis shouldn't technically be allowed but we've seen a few cases of\\nit in existing repositories on github.com so let's just deal with\\nit.\",\n" +
      "        \"tree\": \"c5a02730f0e11c91859094f787e24ce79aa9ff97\",\n" +
      "        \"committer\": {\n" +
      "            \"name\": \"Ryan Tomayko\",\n" +
      "            \"login\": \"rtomayko\",\n" +
      "            \"email\": \"rtomayko@gmail.com\"\n" +
      "        }\n" +
      "    },\n" +
      "    {\n" +
      "        \"parents\": [{\n" +
      "            \"id\": \"cb2644c4e8b472b7d4021ccf6e6d5496a29269b0\"\n" +
      "        },\n" +
      "        {\n" +
      "            \"id\": \"8e2715471201a764bb172775dd9892e469c6fc28\"\n" +
      "        }],\n" +
      "        \"author\": {\n" +
      "            \"name\": \"Aman Gupta\",\n" +
      "            \"login\": \"tmm1\",\n" +
      "            \"email\": \"aman@tmm1.net\"\n" +
      "        },\n" +
      "        \"url\": \"/mojombo/grit/commit/568935ddda3168ddbe876163ace842eeb3d62e0a\",\n" +
      "        \"id\": \"568935ddda3168ddbe876163ace842eeb3d62e0a\",\n" +
      "        \"committed_date\": \"2011-07-01T16:02:45-07:00\",\n" +
      "        \"authored_date\": \"2011-07-01T16:02:45-07:00\",\n" +
      "        \"message\": \"Merge pull request #78 from kevinsawicki/patch-1\\n\\nFix typo in tree method doc\",\n" +
      "        \"tree\": \"e94a442e94304a68712ca64ca75a5f66a592c954\",\n" +
      "        \"committer\": {\n" +
      "            \"name\": \"Aman Gupta\",\n" +
      "            \"login\": \"tmm1\",\n" +
      "            \"email\": \"aman@tmm1.net\"\n" +
      "        }\n" +
      "    },\n" +
      "    {\n" +
      "        \"parents\": [{\n" +
      "            \"id\": \"cb2644c4e8b472b7d4021ccf6e6d5496a29269b0\"\n" +
      "        }],\n" +
      "        \"author\": {\n" +
      "            \"name\": \"Kevin Sawicki\",\n" +
      "            \"login\": \"kevinsawicki\",\n" +
      "            \"email\": \"kevin@github.com\"\n" +
      "        },\n" +
      "        \"url\": \"/mojombo/grit/commit/8e2715471201a764bb172775dd9892e469c6fc28\",\n" +
      "        \"id\": \"8e2715471201a764bb172775dd9892e469c6fc28\",\n" +
      "        \"committed_date\": \"2011-07-01T09:59:50-07:00\",\n" +
      "        \"authored_date\": \"2011-07-01T09:59:50-07:00\",\n" +
      "        \"message\": \"Fix typo in tree method doc\",\n" +
      "        \"tree\": \"e94a442e94304a68712ca64ca75a5f66a592c954\",\n" +
      "        \"committer\": {\n" +
      "            \"name\": \"Kevin Sawicki\",\n" +
      "            \"login\": \"kevinsawicki\",\n" +
      "            \"email\": \"kevin@github.com\"\n" +
      "        }\n" +
      "    },\n" +
      "    {\n" +
      "        \"parents\": [{\n" +
      "            \"id\": \"e1160265209b7add18657e5e19841aec6b07853c\"\n" +
      "        }],\n" +
      "        \"author\": {\n" +
      "            \"name\": \"Ryan Tomayko\",\n" +
      "            \"login\": \"rtomayko\",\n" +
      "            \"email\": \"rtomayko@gmail.com\"\n" +
      "        },\n" +
      "        \"url\": \"/mojombo/grit/commit/cb2644c4e8b472b7d4021ccf6e6d5496a29269b0\",\n" +
      "        \"id\": \"cb2644c4e8b472b7d4021ccf6e6d5496a29269b0\",\n" +
      "        \"committed_date\": \"2011-06-22T00:00:02-07:00\",\n" +
      "        \"authored_date\": \"2011-06-22T00:00:02-07:00\",\n" +
      "        \"message\": \"remove tests for stuff @schacon removed\",\n" +
      "        \"tree\": \"fd7e741a86d867c31e95042203747d9294e0e62e\",\n" +
      "        \"committer\": {\n" +
      "            \"name\": \"Ryan Tomayko\",\n" +
      "            \"login\": \"rtomayko\",\n" +
      "            \"email\": \"rtomayko@gmail.com\"\n" +
      "        }\n" +
      "    },\n" +
      "    {\n" +
      "        \"parents\": [{\n" +
      "            \"id\": \"f19c39b117036834c372f956fd5f466d576120f7\"\n" +
      "        }],\n" +
      "        \"author\": {\n" +
      "            \"name\": \"Ryan Tomayko\",\n" +
      "            \"login\": \"rtomayko\",\n" +
      "            \"email\": \"rtomayko@gmail.com\"\n" +
      "        },\n" +
      "        \"url\": \"/mojombo/grit/commit/e1160265209b7add18657e5e19841aec6b07853c\",\n" +
      "        \"id\": \"e1160265209b7add18657e5e19841aec6b07853c\",\n" +
      "        \"committed_date\": \"2011-06-21T23:57:47-07:00\",\n" +
      "        \"authored_date\": \"2011-06-21T23:21:47-07:00\",\n" +
      "        \"message\": \"Grit::Git check_applies / patch related methods take command hash\\n\\nThis lets us pass an :env so we can use GIT_ALTERNATE_OBJECT_DIRECTORIES\\nto check if a commit applies across repositories.\",\n" +
      "        \"tree\": \"7cfc1f0f4151a31552c71aa9eb17c53d77e92fd8\",\n" +
      "        \"committer\": {\n" +
      "            \"name\": \"Ryan Tomayko\",\n" +
      "            \"login\": \"rtomayko\",\n" +
      "            \"email\": \"rtomayko@gmail.com\"\n" +
      "        }\n" +
      "    },\n" +
      "    {\n" +
      "        \"parents\": [{\n" +
      "            \"id\": \"f38eecb1c9a1cd50b5f535a75a5c56e45cd64456\"\n" +
      "        }],\n" +
      "        \"author\": {\n" +
      "            \"name\": \"Scott Chacon\",\n" +
      "            \"login\": \"schacon\",\n" +
      "            \"email\": \"schacon@gmail.com\"\n" +
      "        },\n" +
      "        \"url\": \"/mojombo/grit/commit/f19c39b117036834c372f956fd5f466d576120f7\",\n" +
      "        \"id\": \"f19c39b117036834c372f956fd5f466d576120f7\",\n" +
      "        \"committed_date\": \"2011-06-21T23:57:39-07:00\",\n" +
      "        \"authored_date\": \"2011-06-16T16:21:05-07:00\",\n" +
      "        \"message\": \"tags api now resty\",\n" +
      "        \"tree\": \"e6b5e92479fdd43a8762c01bd459b87900a25bec\",\n" +
      "        \"committer\": {\n" +
      "            \"name\": \"Ryan Tomayko\",\n" +
      "            \"login\": \"rtomayko\",\n" +
      "            \"email\": \"rtomayko@gmail.com\"\n" +
      "        }\n" +
      "    },\n" +
      "    {\n" +
      "        \"parents\": [{\n" +
      "            \"id\": \"67fc8d200884709a1ce49d1c752414726e488f4a\"\n" +
      "        }],\n" +
      "        \"author\": {\n" +
      "            \"name\": \"Scott Chacon\",\n" +
      "            \"login\": \"schacon\",\n" +
      "            \"email\": \"schacon@gmail.com\"\n" +
      "        },\n" +
      "        \"url\": \"/mojombo/grit/commit/f38eecb1c9a1cd50b5f535a75a5c56e45cd64456\",\n" +
      "        \"id\": \"f38eecb1c9a1cd50b5f535a75a5c56e45cd64456\",\n" +
      "        \"committed_date\": \"2011-06-21T23:57:39-07:00\",\n" +
      "        \"authored_date\": \"2011-06-16T15:11:15-07:00\",\n" +
      "        \"message\": \"start using the GitRuby::GitObject types in the Git data api\",\n" +
      "        \"tree\": \"1ab379180a54f7d9e57a1c84ae16005b92d2c307\",\n" +
      "        \"committer\": {\n" +
      "            \"name\": \"Ryan Tomayko\",\n" +
      "            \"login\": \"rtomayko\",\n" +
      "            \"email\": \"rtomayko@gmail.com\"\n" +
      "        }\n" +
      "    },\n" +
      "    {\n" +
      "        \"parents\": [{\n" +
      "            \"id\": \"b0135670e0002fee8491ea1e15e7308817e9a255\"\n" +
      "        }],\n" +
      "        \"author\": {\n" +
      "            \"name\": \"Scott Chacon\",\n" +
      "            \"login\": \"schacon\",\n" +
      "            \"email\": \"schacon@gmail.com\"\n" +
      "        },\n" +
      "        \"url\": \"/mojombo/grit/commit/67fc8d200884709a1ce49d1c752414726e488f4a\",\n" +
      "        \"id\": \"67fc8d200884709a1ce49d1c752414726e488f4a\",\n" +
      "        \"committed_date\": \"2011-06-21T23:57:39-07:00\",\n" +
      "        \"authored_date\": \"2011-06-16T15:04:15-07:00\",\n" +
      "        \"message\": \"we are not using these anymore. they are confusing\",\n" +
      "        \"tree\": \"da4b0abe055c71132e8166e3c94ef562a9847d7e\",\n" +
      "        \"committer\": {\n" +
      "            \"name\": \"Ryan Tomayko\",\n" +
      "            \"login\": \"rtomayko\",\n" +
      "            \"email\": \"rtomayko@gmail.com\"\n" +
      "        }\n" +
      "    },\n" +
      "    {\n" +
      "        \"parents\": [{\n" +
      "            \"id\": \"1c03f3e1f822232aeb00833081418391a44fe3df\"\n" +
      "        }],\n" +
      "        \"author\": {\n" +
      "            \"name\": \"Ryan Tomayko\",\n" +
      "            \"login\": \"rtomayko\",\n" +
      "            \"email\": \"rtomayko@gmail.com\"\n" +
      "        },\n" +
      "        \"url\": \"/mojombo/grit/commit/b0135670e0002fee8491ea1e15e7308817e9a255\",\n" +
      "        \"id\": \"b0135670e0002fee8491ea1e15e7308817e9a255\",\n" +
      "        \"committed_date\": \"2011-06-15T12:30:25-07:00\",\n" +
      "        \"authored_date\": \"2011-06-15T12:29:14-07:00\",\n" +
      "        \"message\": \"ruby rev_list passes --verify to native rev_parse in fallback\\n\\nOtherwise, the git-rev-parse will return whatever is given as an arg\\nwhen the ref doesn't exist. e.g.,\\n\\n  $ git rev-parse some-bad-ref\\n  some-bad-ref\\n  fatal: ambiguous argument 'some-bad-ref': unknown revision or path not in the working tree.\\n\\nThe error message is on stderr and git-rev-parse exits with non-zero\\nbut the ref name is still output.\\n\\nThe problem here is that code often calls rev_list like:\\n\\n    git.rev_list({}, \\\"some-bad-ref\\\")\\n\\nThen rev_list tries to convert some-bad-ref to a SHA1, gets back the\\nref string, but continues on anyway. This eventually results in the\\nrev_list failing to look up the object because it assumes its a SHA1\\nwhen its really a ref string.\",\n" +
      "        \"tree\": \"1ee950e8a6c7228b66cbbe6d9e891b78b41c958c\",\n" +
      "        \"committer\": {\n" +
      "            \"name\": \"Ryan Tomayko\",\n" +
      "            \"login\": \"rtomayko\",\n" +
      "            \"email\": \"rtomayko@gmail.com\"\n" +
      "        }\n" +
      "    },\n" +
      "    {\n" +
      "        \"parents\": [{\n" +
      "            \"id\": \"a69f33da65a6273570d83a8830a4ae0a2dbebd56\"\n" +
      "        },\n" +
      "        {\n" +
      "            \"id\": \"1fa27b9c8a94909441842fe2d2c2e5843bd564b4\"\n" +
      "        }],\n" +
      "        \"author\": {\n" +
      "            \"name\": \"Ryan Tomayko\",\n" +
      "            \"login\": \"rtomayko\",\n" +
      "            \"email\": \"rtomayko@gmail.com\"\n" +
      "        },\n" +
      "        \"url\": \"/mojombo/grit/commit/1c03f3e1f822232aeb00833081418391a44fe3df\",\n" +
      "        \"id\": \"1c03f3e1f822232aeb00833081418391a44fe3df\",\n" +
      "        \"committed_date\": \"2011-06-10T01:31:23-07:00\",\n" +
      "        \"authored_date\": \"2011-06-10T01:31:23-07:00\",\n" +
      "        \"message\": \"Merge pull request #71 from injekt/master\\n\\nFix warnings on 1.9\",\n" +
      "        \"tree\": \"e0f48a1d63bc947d9f217858670b3d259653adb7\",\n" +
      "        \"committer\": {\n" +
      "            \"name\": \"Ryan Tomayko\",\n" +
      "            \"login\": \"rtomayko\",\n" +
      "            \"email\": \"rtomayko@gmail.com\"\n" +
      "        }\n" +
      "    },\n" +
      "    {\n" +
      "        \"parents\": [{\n" +
      "            \"id\": \"d4ed1ebe6425a0da4dcc57535b4a59a31859a1f5\"\n" +
      "        },\n" +
      "        {\n" +
      "            \"id\": \"23c4b5a384261fdfb706a656e93261e885487903\"\n" +
      "        }],\n" +
      "        \"author\": {\n" +
      "            \"name\": \"Ryan Tomayko\",\n" +
      "            \"login\": \"rtomayko\",\n" +
      "            \"email\": \"rtomayko@gmail.com\"\n" +
      "        },\n" +
      "        \"url\": \"/mojombo/grit/commit/a69f33da65a6273570d83a8830a4ae0a2dbebd56\",\n" +
      "        \"id\": \"a69f33da65a6273570d83a8830a4ae0a2dbebd56\",\n" +
      "        \"committed_date\": \"2011-06-10T01:30:59-07:00\",\n" +
      "        \"authored_date\": \"2011-06-10T01:30:59-07:00\",\n" +
      "        \"message\": \"Merge pull request #72 from cesario/master\\n\\nFix the gemspec\",\n" +
      "        \"tree\": \"ccff6f690e343ab300a6e8e0a7a90d3e36c330ee\",\n" +
      "        \"committer\": {\n" +
      "            \"name\": \"Ryan Tomayko\",\n" +
      "            \"login\": \"rtomayko\",\n" +
      "            \"email\": \"rtomayko@gmail.com\"\n" +
      "        }\n" +
      "    },\n" +
      "    {\n" +
      "        \"parents\": [{\n" +
      "            \"id\": \"d4ed1ebe6425a0da4dcc57535b4a59a31859a1f5\"\n" +
      "        }],\n" +
      "        \"author\": {\n" +
      "            \"name\": \"Franck Verrot\",\n" +
      "            \"login\": \"cesario\",\n" +
      "            \"email\": \"franck@verrot.fr\"\n" +
      "        },\n" +
      "        \"url\": \"/mojombo/grit/commit/23c4b5a384261fdfb706a656e93261e885487903\",\n" +
      "        \"id\": \"23c4b5a384261fdfb706a656e93261e885487903\",\n" +
      "        \"committed_date\": \"2011-06-09T23:37:20-07:00\",\n" +
      "        \"authored_date\": \"2011-06-09T23:37:20-07:00\",\n" +
      "        \"message\": \"Remove missing files from gemspec\",\n" +
      "        \"tree\": \"ccff6f690e343ab300a6e8e0a7a90d3e36c330ee\",\n" +
      "        \"committer\": {\n" +
      "            \"name\": \"Franck Verrot\",\n" +
      "            \"login\": \"cesario\",\n" +
      "            \"email\": \"franck@verrot.fr\"\n" +
      "        }\n" +
      "    },\n" +
      "    {\n" +
      "        \"parents\": [{\n" +
      "            \"id\": \"bd908d7963c297d0e8b957014e534bffeeb63d44\"\n" +
      "        }],\n" +
      "        \"author\": {\n" +
      "            \"name\": \"Lee Jarvis\",\n" +
      "            \"login\": \"injekt\",\n" +
      "            \"email\": \"lee@jarvis.co\"\n" +
      "        },\n" +
      "        \"url\": \"/mojombo/grit/commit/1fa27b9c8a94909441842fe2d2c2e5843bd564b4\",\n" +
      "        \"id\": \"1fa27b9c8a94909441842fe2d2c2e5843bd564b4\",\n" +
      "        \"committed_date\": \"2011-06-09T13:45:38-07:00\",\n" +
      "        \"authored_date\": \"2011-06-09T13:45:38-07:00\",\n" +
      "        \"message\": \"remove commented out lazy_reader\",\n" +
      "        \"tree\": \"c80880124ce5b734169241cdacc3fe14b9d81242\",\n" +
      "        \"committer\": {\n" +
      "            \"name\": \"Lee Jarvis\",\n" +
      "            \"login\": \"injekt\",\n" +
      "            \"email\": \"lee@jarvis.co\"\n" +
      "        }\n" +
      "    },\n" +
      "    {\n" +
      "        \"parents\": [{\n" +
      "            \"id\": \"d4ed1ebe6425a0da4dcc57535b4a59a31859a1f5\"\n" +
      "        }],\n" +
      "        \"author\": {\n" +
      "            \"name\": \"Lee Jarvis\",\n" +
      "            \"login\": \"injekt\",\n" +
      "            \"email\": \"lee@jarvis.co\"\n" +
      "        },\n" +
      "        \"url\": \"/mojombo/grit/commit/bd908d7963c297d0e8b957014e534bffeeb63d44\",\n" +
      "        \"id\": \"bd908d7963c297d0e8b957014e534bffeeb63d44\",\n" +
      "        \"committed_date\": \"2011-06-09T06:58:28-07:00\",\n" +
      "        \"authored_date\": \"2011-06-09T06:58:28-07:00\",\n" +
      "        \"message\": \"fix warnings on Ruby 1.9\",\n" +
      "        \"tree\": \"6593657976c5d083daa3921f0657e519b41e7f45\",\n" +
      "        \"committer\": {\n" +
      "            \"name\": \"Lee Jarvis\",\n" +
      "            \"login\": \"injekt\",\n" +
      "            \"email\": \"lee@jarvis.co\"\n" +
      "        }\n" +
      "    },\n" +
      "    {\n" +
      "        \"parents\": [{\n" +
      "            \"id\": \"9042243dc486031bb1eb3427ece27db18a06e234\"\n" +
      "        }],\n" +
      "        \"author\": {\n" +
      "            \"name\": \"Ryan Tomayko\",\n" +
      "            \"login\": \"rtomayko\",\n" +
      "            \"email\": \"rtomayko@gmail.com\"\n" +
      "        },\n" +
      "        \"url\": \"/mojombo/grit/commit/d4ed1ebe6425a0da4dcc57535b4a59a31859a1f5\",\n" +
      "        \"id\": \"d4ed1ebe6425a0da4dcc57535b4a59a31859a1f5\",\n" +
      "        \"committed_date\": \"2011-06-08T11:17:18-07:00\",\n" +
      "        \"authored_date\": \"2011-06-08T11:17:18-07:00\",\n" +
      "        \"message\": \"get rid of sample hooks in test fixture repo\",\n" +
      "        \"tree\": \"18258b325cfe73df2c35c36f8085727e6a1a164a\",\n" +
      "        \"committer\": {\n" +
      "            \"name\": \"Ryan Tomayko\",\n" +
      "            \"login\": \"rtomayko\",\n" +
      "            \"email\": \"rtomayko@gmail.com\"\n" +
      "        }\n" +
      "    },\n" +
      "    {\n" +
      "        \"parents\": [{\n" +
      "            \"id\": \"e4d4461be91a202591e8f401365c047e5300faef\"\n" +
      "        },\n" +
      "        {\n" +
      "            \"id\": \"087676cd79e4fca4fb14c7baa8e072dabc6da383\"\n" +
      "        }],\n" +
      "        \"author\": {\n" +
      "            \"name\": \"Ryan Tomayko\",\n" +
      "            \"login\": \"rtomayko\",\n" +
      "            \"email\": \"rtomayko@gmail.com\"\n" +
      "        },\n" +
      "        \"url\": \"/mojombo/grit/commit/9042243dc486031bb1eb3427ece27db18a06e234\",\n" +
      "        \"id\": \"9042243dc486031bb1eb3427ece27db18a06e234\",\n" +
      "        \"committed_date\": \"2011-06-08T11:05:43-07:00\",\n" +
      "        \"authored_date\": \"2011-06-08T11:05:43-07:00\",\n" +
      "        \"message\": \"Merge remote branch 'origin/github'\",\n" +
      "        \"tree\": \"12c0fddeb08fe0484069ba7239355c63a3514b79\",\n" +
      "        \"committer\": {\n" +
      "            \"name\": \"Ryan Tomayko\",\n" +
      "            \"login\": \"rtomayko\",\n" +
      "            \"email\": \"rtomayko@gmail.com\"\n" +
      "        }\n" +
      "    },\n" +
      "    {\n" +
      "        \"parents\": [{\n" +
      "            \"id\": \"f5838446fb32356548eaa03f374098cabe8d5a94\"\n" +
      "        }],\n" +
      "        \"author\": {\n" +
      "            \"name\": \"Aman Gupta\",\n" +
      "            \"login\": \"tmm1\",\n" +
      "            \"email\": \"aman@tmm1.net\"\n" +
      "        },\n" +
      "        \"url\": \"/mojombo/grit/commit/e4d4461be91a202591e8f401365c047e5300faef\",\n" +
      "        \"id\": \"e4d4461be91a202591e8f401365c047e5300faef\",\n" +
      "        \"committed_date\": \"2011-06-08T11:01:35-07:00\",\n" +
      "        \"authored_date\": \"2011-06-08T11:01:35-07:00\",\n" +
      "        \"message\": \"bump posix-spawn dependency to latest w/ jruby support\",\n" +
      "        \"tree\": \"c081954188ae2366e19ad9990345e47f5ffc4893\",\n" +
      "        \"committer\": {\n" +
      "            \"name\": \"Aman Gupta\",\n" +
      "            \"login\": \"tmm1\",\n" +
      "            \"email\": \"aman@tmm1.net\"\n" +
      "        }\n" +
      "    },\n" +
      "    {\n" +
      "        \"parents\": [{\n" +
      "            \"id\": \"1b2fe779964fda9aaa9f417e371923db4a396b22\"\n" +
      "        },\n" +
      "        {\n" +
      "            \"id\": \"0822ea6aeef2fc0807688c5b7f0161c1c0577655\"\n" +
      "        }],\n" +
      "        \"author\": {\n" +
      "            \"name\": \"Aman Gupta\",\n" +
      "            \"login\": \"tmm1\",\n" +
      "            \"email\": \"aman@tmm1.net\"\n" +
      "        },\n" +
      "        \"url\": \"/mojombo/grit/commit/f5838446fb32356548eaa03f374098cabe8d5a94\",\n" +
      "        \"id\": \"f5838446fb32356548eaa03f374098cabe8d5a94\",\n" +
      "        \"committed_date\": \"2011-06-08T11:01:08-07:00\",\n" +
      "        \"authored_date\": \"2011-06-08T11:01:08-07:00\",\n" +
      "        \"message\": \"Merge branch 'posix-spawn'\",\n" +
      "        \"tree\": \"7bf76195c4a96f4a23155b28e9538d1da7d8ec8c\",\n" +
      "        \"committer\": {\n" +
      "            \"name\": \"Aman Gupta\",\n" +
      "            \"login\": \"tmm1\",\n" +
      "            \"email\": \"aman@tmm1.net\"\n" +
      "        }\n" +
      "    },\n" +
      "    {\n" +
      "        \"parents\": [{\n" +
      "            \"id\": \"12c98749256a099bde2e328b0c943dc2f5bdca7c\"\n" +
      "        }],\n" +
      "        \"author\": {\n" +
      "            \"name\": \"Aman Gupta\",\n" +
      "            \"login\": \"tmm1\",\n" +
      "            \"email\": \"aman@tmm1.net\"\n" +
      "        },\n" +
      "        \"url\": \"/mojombo/grit/commit/0822ea6aeef2fc0807688c5b7f0161c1c0577655\",\n" +
      "        \"id\": \"0822ea6aeef2fc0807688c5b7f0161c1c0577655\",\n" +
      "        \"committed_date\": \"2011-06-08T10:59:54-07:00\",\n" +
      "        \"authored_date\": \"2011-06-08T10:59:54-07:00\",\n" +
      "        \"message\": \"remove all the grit jruby hacks in favor of updated posix-spawn\",\n" +
      "        \"tree\": \"ff097d18a43a7ce7c43a968a1f4e880740e5b2a6\",\n" +
      "        \"committer\": {\n" +
      "            \"name\": \"Aman Gupta\",\n" +
      "            \"login\": \"tmm1\",\n" +
      "            \"email\": \"aman@tmm1.net\"\n" +
      "        }\n" +
      "    },\n" +
      "    {\n" +
      "        \"parents\": [{\n" +
      "            \"id\": \"f6dd62114b46b3700b4c06d2c8e2437560faecf4\"\n" +
      "        },\n" +
      "        {\n" +
      "            \"id\": \"bc628b07a6415de47cd10f22cb5af2f54e9ef29e\"\n" +
      "        }],\n" +
      "        \"author\": {\n" +
      "            \"name\": \"Tom Preston-Werner\",\n" +
      "            \"login\": \"mojombo\",\n" +
      "            \"email\": \"tom@mojombo.com\"\n" +
      "        },\n" +
      "        \"url\": \"/mojombo/grit/commit/087676cd79e4fca4fb14c7baa8e072dabc6da383\",\n" +
      "        \"id\": \"087676cd79e4fca4fb14c7baa8e072dabc6da383\",\n" +
      "        \"committed_date\": \"2011-06-08T10:59:41-07:00\",\n" +
      "        \"authored_date\": \"2011-06-08T10:59:41-07:00\",\n" +
      "        \"message\": \"Merge branch 'patch-id' into github\",\n" +
      "        \"tree\": \"74e3877eb439c34ef88c5086cb360cf525c446d0\",\n" +
      "        \"committer\": {\n" +
      "            \"name\": \"Tom Preston-Werner\",\n" +
      "            \"login\": \"mojombo\",\n" +
      "            \"email\": \"tom@mojombo.com\"\n" +
      "        }\n" +
      "    },\n" +
      "    {\n" +
      "        \"parents\": [{\n" +
      "            \"id\": \"1d2e07ca0be00062fa77b3d916c6293456b81939\"\n" +
      "        }],\n" +
      "        \"author\": {\n" +
      "            \"name\": \"Scott Chacon\",\n" +
      "            \"login\": \"schacon\",\n" +
      "            \"email\": \"schacon@gmail.com\"\n" +
      "        },\n" +
      "        \"url\": \"/mojombo/grit/commit/f6dd62114b46b3700b4c06d2c8e2437560faecf4\",\n" +
      "        \"id\": \"f6dd62114b46b3700b4c06d2c8e2437560faecf4\",\n" +
      "        \"committed_date\": \"2011-06-08T10:45:50-07:00\",\n" +
      "        \"authored_date\": \"2011-05-24T16:04:35-07:00\",\n" +
      "        \"message\": \"tree reading and writing\",\n" +
      "        \"tree\": \"76d16620c74c05f15f9def35ff85ac60079cc888\",\n" +
      "        \"committer\": {\n" +
      "            \"name\": \"Ryan Tomayko\",\n" +
      "            \"login\": \"rtomayko\",\n" +
      "            \"email\": \"rtomayko@gmail.com\"\n" +
      "        }\n" +
      "    },\n" +
      "    {\n" +
      "        \"parents\": [{\n" +
      "            \"id\": \"a127face30e11735c8864fba33ae7265e1291c0b\"\n" +
      "        }],\n" +
      "        \"author\": {\n" +
      "            \"name\": \"Scott Chacon\",\n" +
      "            \"login\": \"schacon\",\n" +
      "            \"email\": \"schacon@gmail.com\"\n" +
      "        },\n" +
      "        \"url\": \"/mojombo/grit/commit/1d2e07ca0be00062fa77b3d916c6293456b81939\",\n" +
      "        \"id\": \"1d2e07ca0be00062fa77b3d916c6293456b81939\",\n" +
      "        \"committed_date\": \"2011-06-08T10:43:14-07:00\",\n" +
      "        \"authored_date\": \"2011-05-27T20:57:15-07:00\",\n" +
      "        \"message\": \"hm, guess we actually need this\",\n" +
      "        \"tree\": \"40e3d915e15b9692e5ade7cfe3e3fad7f69aa29e\",\n" +
      "        \"committer\": {\n" +
      "            \"name\": \"Ryan Tomayko\",\n" +
      "            \"login\": \"rtomayko\",\n" +
      "            \"email\": \"rtomayko@gmail.com\"\n" +
      "        }\n" +
      "    },\n" +
      "    {\n" +
      "        \"parents\": [{\n" +
      "            \"id\": \"278750d531a1879b38d323e03fba727f9ca6f3ae\"\n" +
      "        }],\n" +
      "        \"author\": {\n" +
      "            \"name\": \"Scott Chacon\",\n" +
      "            \"login\": \"schacon\",\n" +
      "            \"email\": \"schacon@gmail.com\"\n" +
      "        },\n" +
      "        \"url\": \"/mojombo/grit/commit/a127face30e11735c8864fba33ae7265e1291c0b\",\n" +
      "        \"id\": \"a127face30e11735c8864fba33ae7265e1291c0b\",\n" +
      "        \"committed_date\": \"2011-06-08T10:43:14-07:00\",\n" +
      "        \"authored_date\": \"2011-05-27T20:46:06-07:00\",\n" +
      "        \"message\": \"reference updating and deleting working\",\n" +
      "        \"tree\": \"b546b1fac331c1ccc5d6830dd1082ea2e91322d4\",\n" +
      "        \"committer\": {\n" +
      "            \"name\": \"Ryan Tomayko\",\n" +
      "            \"login\": \"rtomayko\",\n" +
      "            \"email\": \"rtomayko@gmail.com\"\n" +
      "        }\n" +
      "    },\n" +
      "    {\n" +
      "        \"parents\": [{\n" +
      "            \"id\": \"0726010d88ff6fae4cd801d32f39cbc355a5c1c0\"\n" +
      "        }],\n" +
      "        \"author\": {\n" +
      "            \"name\": \"Scott Chacon\",\n" +
      "            \"login\": \"schacon\",\n" +
      "            \"email\": \"schacon@gmail.com\"\n" +
      "        },\n" +
      "        \"url\": \"/mojombo/grit/commit/278750d531a1879b38d323e03fba727f9ca6f3ae\",\n" +
      "        \"id\": \"278750d531a1879b38d323e03fba727f9ca6f3ae\",\n" +
      "        \"committed_date\": \"2011-06-08T10:43:14-07:00\",\n" +
      "        \"authored_date\": \"2011-05-27T11:36:20-07:00\",\n" +
      "        \"message\": \"git refs api read stuff working and tested\",\n" +
      "        \"tree\": \"b6119332de01b26c3a406d32d635ff55ef839c7a\",\n" +
      "        \"committer\": {\n" +
      "            \"name\": \"Ryan Tomayko\",\n" +
      "            \"login\": \"rtomayko\",\n" +
      "            \"email\": \"rtomayko@gmail.com\"\n" +
      "        }\n" +
      "    },\n" +
      "    {\n" +
      "        \"parents\": [{\n" +
      "            \"id\": \"7a39c57d448be10116af5c328ed98f7dbf45c810\"\n" +
      "        }],\n" +
      "        \"author\": {\n" +
      "            \"name\": \"Scott Chacon\",\n" +
      "            \"login\": \"schacon\",\n" +
      "            \"email\": \"schacon@gmail.com\"\n" +
      "        },\n" +
      "        \"url\": \"/mojombo/grit/commit/0726010d88ff6fae4cd801d32f39cbc355a5c1c0\",\n" +
      "        \"id\": \"0726010d88ff6fae4cd801d32f39cbc355a5c1c0\",\n" +
      "        \"committed_date\": \"2011-06-08T10:43:14-07:00\",\n" +
      "        \"authored_date\": \"2011-05-27T09:11:07-07:00\",\n" +
      "        \"message\": \"tag creation and tests\",\n" +
      "        \"tree\": \"39971418cff4616d6b4e1fecf194a49fca8b4ea0\",\n" +
      "        \"committer\": {\n" +
      "            \"name\": \"Ryan Tomayko\",\n" +
      "            \"login\": \"rtomayko\",\n" +
      "            \"email\": \"rtomayko@gmail.com\"\n" +
      "        }\n" +
      "    },\n" +
      "    {\n" +
      "        \"parents\": [{\n" +
      "            \"id\": \"06287377b98dac094e7b42cd1291821d33e4b93c\"\n" +
      "        }],\n" +
      "        \"author\": {\n" +
      "            \"name\": \"Scott Chacon\",\n" +
      "            \"login\": \"schacon\",\n" +
      "            \"email\": \"schacon@gmail.com\"\n" +
      "        },\n" +
      "        \"url\": \"/mojombo/grit/commit/7a39c57d448be10116af5c328ed98f7dbf45c810\",\n" +
      "        \"id\": \"7a39c57d448be10116af5c328ed98f7dbf45c810\",\n" +
      "        \"committed_date\": \"2011-06-08T10:43:14-07:00\",\n" +
      "        \"authored_date\": \"2011-05-27T08:17:40-07:00\",\n" +
      "        \"message\": \"updates to grit for tag api\",\n" +
      "        \"tree\": \"a1675222c2685ce8fa6b638dbdb4f6d71514bf2b\",\n" +
      "        \"committer\": {\n" +
      "            \"name\": \"Ryan Tomayko\",\n" +
      "            \"login\": \"rtomayko\",\n" +
      "            \"email\": \"rtomayko@gmail.com\"\n" +
      "        }\n" +
      "    },\n" +
      "    {\n" +
      "        \"parents\": [{\n" +
      "            \"id\": \"2bedb05d27dc7da5be97aa7c9d2fa5a2da81d2bd\"\n" +
      "        }],\n" +
      "        \"author\": {\n" +
      "            \"name\": \"Scott Chacon\",\n" +
      "            \"login\": \"schacon\",\n" +
      "            \"email\": \"schacon@gmail.com\"\n" +
      "        },\n" +
      "        \"url\": \"/mojombo/grit/commit/06287377b98dac094e7b42cd1291821d33e4b93c\",\n" +
      "        \"id\": \"06287377b98dac094e7b42cd1291821d33e4b93c\",\n" +
      "        \"committed_date\": \"2011-06-08T10:43:13-07:00\",\n" +
      "        \"authored_date\": \"2011-05-26T10:29:36-07:00\",\n" +
      "        \"message\": \"commit listing and writing api\",\n" +
      "        \"tree\": \"38aa9901e0f760e60ec69943f8e647e0d8c10a8e\",\n" +
      "        \"committer\": {\n" +
      "            \"name\": \"Ryan Tomayko\",\n" +
      "            \"login\": \"rtomayko\",\n" +
      "            \"email\": \"rtomayko@gmail.com\"\n" +
      "        }\n" +
      "    },\n" +
      "    {\n" +
      "        \"parents\": [{\n" +
      "            \"id\": \"7fbeffcd53a2f0960d06ab26590cda11567ef99d\"\n" +
      "        }],\n" +
      "        \"author\": {\n" +
      "            \"name\": \"Scott Chacon\",\n" +
      "            \"login\": \"schacon\",\n" +
      "            \"email\": \"schacon@gmail.com\"\n" +
      "        },\n" +
      "        \"url\": \"/mojombo/grit/commit/2bedb05d27dc7da5be97aa7c9d2fa5a2da81d2bd\",\n" +
      "        \"id\": \"2bedb05d27dc7da5be97aa7c9d2fa5a2da81d2bd\",\n" +
      "        \"committed_date\": \"2011-06-08T10:43:13-07:00\",\n" +
      "        \"authored_date\": \"2011-05-25T20:46:42-07:00\",\n" +
      "        \"message\": \"write_tree can take a sha for a tree and can return the last tree size via an instance var\",\n" +
      "        \"tree\": \"6c9adf4ae175732b24204038f79d278099940779\",\n" +
      "        \"committer\": {\n" +
      "            \"name\": \"Ryan Tomayko\",\n" +
      "            \"login\": \"rtomayko\",\n" +
      "            \"email\": \"rtomayko@gmail.com\"\n" +
      "        }\n" +
      "    },\n" +
      "    {\n" +
      "        \"parents\": [{\n" +
      "            \"id\": \"7fbeffcd53a2f0960d06ab26590cda11567ef99d\"\n" +
      "        }],\n" +
      "        \"author\": {\n" +
      "            \"name\": \"Tom Preston-Werner\",\n" +
      "            \"login\": \"mojombo\",\n" +
      "            \"email\": \"tom@mojombo.com\"\n" +
      "        },\n" +
      "        \"url\": \"/mojombo/grit/commit/bc628b07a6415de47cd10f22cb5af2f54e9ef29e\",\n" +
      "        \"id\": \"bc628b07a6415de47cd10f22cb5af2f54e9ef29e\",\n" +
      "        \"committed_date\": \"2011-05-31T17:04:04-07:00\",\n" +
      "        \"authored_date\": \"2011-05-31T17:04:04-07:00\",\n" +
      "        \"message\": \"Add Commit#patch_id.\",\n" +
      "        \"tree\": \"44878def246389b344fa84f758f16d1c74a4824b\",\n" +
      "        \"committer\": {\n" +
      "            \"name\": \"Tom Preston-Werner\",\n" +
      "            \"login\": \"mojombo\",\n" +
      "            \"email\": \"tom@mojombo.com\"\n" +
      "        }\n" +
      "    },\n" +
      "    {\n" +
      "        \"parents\": [{\n" +
      "            \"id\": \"084d5cf695ee072d4335740971771180f347f9ff\"\n" +
      "        },\n" +
      "        {\n" +
      "            \"id\": \"f805ff0287d18eb8ba28c38ab6ac0d1593021988\"\n" +
      "        }],\n" +
      "        \"author\": {\n" +
      "            \"name\": \"Ryan Tomayko\",\n" +
      "            \"login\": \"rtomayko\",\n" +
      "            \"email\": \"rtomayko@gmail.com\"\n" +
      "        },\n" +
      "        \"url\": \"/mojombo/grit/commit/1b2fe779964fda9aaa9f417e371923db4a396b22\",\n" +
      "        \"id\": \"1b2fe779964fda9aaa9f417e371923db4a396b22\",\n" +
      "        \"committed_date\": \"2011-05-30T15:24:54-07:00\",\n" +
      "        \"authored_date\": \"2011-05-30T15:24:54-07:00\",\n" +
      "        \"message\": \"Merge pull request #60 from dkowis/fixing-spaces\\n\\nFix for files with leading or trailing spaces\",\n" +
      "        \"tree\": \"a7dc09674e51427181c3743065a48ece055cc18c\",\n" +
      "        \"committer\": {\n" +
      "            \"name\": \"Ryan Tomayko\",\n" +
      "            \"login\": \"rtomayko\",\n" +
      "            \"email\": \"rtomayko@gmail.com\"\n" +
      "        }\n" +
      "    },\n" +
      "    {\n" +
      "        \"parents\": [{\n" +
      "            \"id\": \"19591b4524d19f216c2933f86b723f759c604b3e\"\n" +
      "        },\n" +
      "        {\n" +
      "            \"id\": \"4c592b4846f7c8755d4707637b195b5d60e3ce6e\"\n" +
      "        }],\n" +
      "        \"author\": {\n" +
      "            \"name\": \"Ryan Tomayko\",\n" +
      "            \"login\": \"rtomayko\",\n" +
      "            \"email\": \"rtomayko@gmail.com\"\n" +
      "        },\n" +
      "        \"url\": \"/mojombo/grit/commit/084d5cf695ee072d4335740971771180f347f9ff\",\n" +
      "        \"id\": \"084d5cf695ee072d4335740971771180f347f9ff\",\n" +
      "        \"committed_date\": \"2011-05-30T15:22:38-07:00\",\n" +
      "        \"authored_date\": \"2011-05-30T15:22:38-07:00\",\n" +
      "        \"message\": \"Merge pull request #68 from bobbyw/master\\n\\nDefault parameter for with_timeout incorrect\",\n" +
      "        \"tree\": \"8ac8088624ef0bcb3a7fca4ac0610b0917da183f\",\n" +
      "        \"committer\": {\n" +
      "            \"name\": \"Ryan Tomayko\",\n" +
      "            \"login\": \"rtomayko\",\n" +
      "            \"email\": \"rtomayko@gmail.com\"\n" +
      "        }\n" +
      "    },\n" +
      "    {\n" +
      "        \"parents\": [{\n" +
      "            \"id\": \"19591b4524d19f216c2933f86b723f759c604b3e\"\n" +
      "        }],\n" +
      "        \"author\": {\n" +
      "            \"name\": \"Bobby Wilson\",\n" +
      "            \"login\": \"bobbyw\",\n" +
      "            \"email\": \"bobbywilson0@gmail.com\"\n" +
      "        },\n" +
      "        \"url\": \"/mojombo/grit/commit/4c592b4846f7c8755d4707637b195b5d60e3ce6e\",\n" +
      "        \"id\": \"4c592b4846f7c8755d4707637b195b5d60e3ce6e\",\n" +
      "        \"committed_date\": \"2011-05-30T11:23:05-07:00\",\n" +
      "        \"authored_date\": \"2011-05-30T11:23:05-07:00\",\n" +
      "        \"message\": \"fix default parameter for with_timeout\",\n" +
      "        \"tree\": \"8ac8088624ef0bcb3a7fca4ac0610b0917da183f\",\n" +
      "        \"committer\": {\n" +
      "            \"name\": \"Bobby Wilson\",\n" +
      "            \"login\": \"bobbyw\",\n" +
      "            \"email\": \"bobbywilson0@gmail.com\"\n" +
      "        }\n" +
      "    },\n" +
      "    {\n" +
      "        \"parents\": [{\n" +
      "            \"id\": \"735a6131b440dc3099b9ef087efb8b095c5b775b\"\n" +
      "        },\n" +
      "        {\n" +
      "            \"id\": \"19591b4524d19f216c2933f86b723f759c604b3e\"\n" +
      "        }],\n" +
      "        \"author\": {\n" +
      "            \"name\": \"Ryan Tomayko\",\n" +
      "            \"login\": \"rtomayko\",\n" +
      "            \"email\": \"rtomayko@gmail.com\"\n" +
      "        },\n" +
      "        \"url\": \"/mojombo/grit/commit/7fbeffcd53a2f0960d06ab26590cda11567ef99d\",\n" +
      "        \"id\": \"7fbeffcd53a2f0960d06ab26590cda11567ef99d\",\n" +
      "        \"committed_date\": \"2011-05-24T10:54:09-07:00\",\n" +
      "        \"authored_date\": \"2011-05-24T10:54:09-07:00\",\n" +
      "        \"message\": \"Merge remote branch 'origin/master' into github\",\n" +
      "        \"tree\": \"98e7efd95f7ae6a2aa3d7123cbfa4758d07818b7\",\n" +
      "        \"committer\": {\n" +
      "            \"name\": \"Ryan Tomayko\",\n" +
      "            \"login\": \"rtomayko\",\n" +
      "            \"email\": \"rtomayko@gmail.com\"\n" +
      "        }\n" +
      "    }]\n" +
      "}"));

    System.out.println(lst);
  }

  public void testTwitterResponse() {
    JSchemaUtils.parseJson("[{\"id_str\":\"151530496705306625\",\"in_reply_to_status_id\":null,\"contributors\":null,\"place\":null,\"truncated\":false,\"geo\":null,\"favorited\":false,\"created_at\":\"Tue Dec 27 05:11:00 +0000 2011\",\"user\":{\"id_str\":\"213626025\",\"contributors_enabled\":false,\"lang\":\"en\",\"protected\":false,\"url\":\"http:\\/\\/calbear.org\",\"default_profile\":true,\"profile_use_background_image\":true,\"name\":\"Carson Gross\",\"default_profile_image\":false,\"friends_count\":14,\"profile_text_color\":\"333333\",\"statuses_count\":774,\"profile_background_image_url\":\"http:\\/\\/a0.twimg.com\\/images\\/themes\\/theme1\\/bg.png\",\"utc_offset\":-28800,\"description\":\"http:\\/\\/gosu-lang.org | http:\\/\\/ronin-web.org | http:\\/\\/vark.github.com ...\\nI like old diesel Land Cruisers.\",\"is_translator\":false,\"created_at\":\"Tue Nov 09 11:45:52 +0000 2010\",\"profile_link_color\":\"0084B4\",\"following\":null,\"profile_background_image_url_https\":\"https:\\/\\/si0.twimg.com\\/images\\/themes\\/theme1\\/bg.png\",\"favourites_count\":0,\"follow_request_sent\":null,\"geo_enabled\":false,\"profile_background_color\":\"C0DEED\",\"profile_background_tile\":false,\"followers_count\":64,\"profile_image_url\":\"http:\\/\\/a1.twimg.com\\/profile_images\\/1163391423\\/me_normal.jpg\",\"screen_name\":\"carson_gross\",\"show_all_inline_media\":false,\"profile_sidebar_fill_color\":\"DDEEF6\",\"profile_image_url_https\":\"https:\\/\\/si0.twimg.com\\/profile_images\\/1163391423\\/me_normal.jpg\",\"listed_count\":6,\"notifications\":null,\"location\":\"Sacramento, California\",\"id\":213626025,\"verified\":false,\"time_zone\":\"Pacific Time (US & Canada)\",\"profile_sidebar_border_color\":\"C0DEED\"},\"in_reply_to_user_id\":76805362,\"retweet_count\":0,\"in_reply_to_screen_name\":\"am2605\",\"in_reply_to_status_id_str\":null,\"retweeted\":false,\"source\":\"web\",\"in_reply_to_user_id_str\":\"76805362\",\"coordinates\":null,\"id\":151530496705306625,\"text\":\"@am2605 BTW, I'm not going to steal his thunder on the mailing list, but brian chang has been doing manly work integrating gosu and maven.\"},{\"id_str\":\"151508754746195968\",\"in_reply_to_status_id\":151502906942693378,\"contributors\":null,\"place\":null,\"truncated\":false,\"geo\":null,\"favorited\":false,\"created_at\":\"Tue Dec 27 03:44:36 +0000 2011\",\"user\":{\"id_str\":\"213626025\",\"contributors_enabled\":false,\"lang\":\"en\",\"protected\":false,\"url\":\"http:\\/\\/calbear.org\",\"profile_use_background_image\":true,\"name\":\"Carson Gross\",\"default_profile_image\":false,\"friends_count\":14,\"profile_text_color\":\"333333\",\"statuses_count\":774,\"profile_background_image_url\":\"http:\\/\\/a0.twimg.com\\/images\\/themes\\/theme1\\/bg.png\",\"utc_offset\":-28800,\"description\":\"http:\\/\\/gosu-lang.org | http:\\/\\/ronin-web.org | http:\\/\\/vark.github.com ...\\nI like old diesel Land Cruisers.\",\"is_translator\":false,\"created_at\":\"Tue Nov 09 11:45:52 +0000 2010\",\"profile_link_color\":\"0084B4\",\"following\":null,\"profile_background_image_url_https\":\"https:\\/\\/si0.twimg.com\\/images\\/themes\\/theme1\\/bg.png\",\"favourites_count\":0,\"follow_request_sent\":null,\"geo_enabled\":false,\"profile_background_color\":\"C0DEED\",\"profile_background_tile\":false,\"followers_count\":64,\"profile_image_url\":\"http:\\/\\/a1.twimg.com\\/profile_images\\/1163391423\\/me_normal.jpg\",\"screen_name\":\"carson_gross\",\"show_all_inline_media\":false,\"profile_sidebar_fill_color\":\"DDEEF6\",\"profile_image_url_https\":\"https:\\/\\/si0.twimg.com\\/profile_images\\/1163391423\\/me_normal.jpg\",\"listed_count\":6,\"notifications\":null,\"location\":\"Sacramento, California\",\"id\":213626025,\"default_profile\":true,\"verified\":false,\"time_zone\":\"Pacific Time (US & Canada)\",\"profile_sidebar_border_color\":\"C0DEED\"},\"in_reply_to_user_id\":190464484,\"retweet_count\":0,\"in_reply_to_screen_name\":\"coryfoo\",\"in_reply_to_status_id_str\":\"151502906942693378\",\"retweeted\":false,\"source\":\"web\",\"in_reply_to_user_id_str\":\"190464484\",\"coordinates\":null,\"id\":151508754746195968,\"text\":\"@coryfoo Well, I should probably write some tests.  ;)  I'll try to get a version up on the gosu m2 tomorrow w\\/ brian.\"},{\"id_str\":\"151499585729216512\",\"in_reply_to_status_id\":151477026463809538,\"contributors\":null,\"place\":null,\"truncated\":false,\"geo\":null,\"favorited\":false,\"created_at\":\"Tue Dec 27 03:08:10 +0000 2011\",\"user\":{\"id_str\":\"213626025\",\"contributors_enabled\":false,\"lang\":\"en\",\"protected\":false,\"url\":\"http:\\/\\/calbear.org\",\"default_profile\":true,\"profile_use_background_image\":true,\"name\":\"Carson Gross\",\"default_profile_image\":false,\"friends_count\":14,\"profile_text_color\":\"333333\",\"statuses_count\":774,\"profile_background_image_url\":\"http:\\/\\/a0.twimg.com\\/images\\/themes\\/theme1\\/bg.png\",\"utc_offset\":-28800,\"description\":\"http:\\/\\/gosu-lang.org | http:\\/\\/ronin-web.org | http:\\/\\/vark.github.com ...\\nI like old diesel Land Cruisers.\",\"is_translator\":false,\"created_at\":\"Tue Nov 09 11:45:52 +0000 2010\",\"profile_link_color\":\"0084B4\",\"following\":null,\"profile_background_image_url_https\":\"https:\\/\\/si0.twimg.com\\/images\\/themes\\/theme1\\/bg.png\",\"favourites_count\":0,\"follow_request_sent\":null,\"geo_enabled\":false,\"profile_background_color\":\"C0DEED\",\"profile_background_tile\":false,\"followers_count\":64,\"profile_image_url\":\"http:\\/\\/a1.twimg.com\\/profile_images\\/1163391423\\/me_normal.jpg\",\"screen_name\":\"carson_gross\",\"show_all_inline_media\":false,\"profile_sidebar_fill_color\":\"DDEEF6\",\"profile_image_url_https\":\"https:\\/\\/si0.twimg.com\\/profile_images\\/1163391423\\/me_normal.jpg\",\"listed_count\":6,\"notifications\":null,\"location\":\"Sacramento, California\",\"id\":213626025,\"verified\":false,\"time_zone\":\"Pacific Time (US & Canada)\",\"profile_sidebar_border_color\":\"C0DEED\"},\"in_reply_to_user_id\":190464484,\"retweet_count\":0,\"in_reply_to_screen_name\":\"coryfoo\",\"in_reply_to_status_id_str\":\"151477026463809538\",\"retweeted\":false,\"source\":\"\\u003Ca href=\\\"http:\\/\\/twitter.com\\/#!\\/download\\/iphone\\\" rel=\\\"nofollow\\\"\\u003ETwitter for iPhone\\u003C\\/a\\u003E\",\"in_reply_to_user_id_str\":\"190464484\",\"coordinates\":null,\"id\":151499585729216512,\"text\":\"@coryfoo Yeah, it'll be sick for Goson\\/JSchema, config code, etc.\"},{\"id_str\":\"151446809196232704\",\"in_reply_to_status_id\":null,\"contributors\":null,\"place\":null,\"truncated\":false,\"geo\":null,\"favorited\":false,\"created_at\":\"Mon Dec 26 23:38:27 +0000 2011\",\"user\":{\"id_str\":\"213626025\",\"contributors_enabled\":false,\"lang\":\"en\",\"protected\":false,\"url\":\"http:\\/\\/calbear.org\",\"profile_use_background_image\":true,\"name\":\"Carson Gross\",\"default_profile_image\":false,\"friends_count\":14,\"profile_text_color\":\"333333\",\"statuses_count\":774,\"profile_background_image_url\":\"http:\\/\\/a0.twimg.com\\/images\\/themes\\/theme1\\/bg.png\",\"utc_offset\":-28800,\"description\":\"http:\\/\\/gosu-lang.org | http:\\/\\/ronin-web.org | http:\\/\\/vark.github.com ...\\nI like old diesel Land Cruisers.\",\"is_translator\":false,\"created_at\":\"Tue Nov 09 11:45:52 +0000 2010\",\"profile_link_color\":\"0084B4\",\"default_profile\":true,\"following\":true,\"profile_background_image_url_https\":\"https:\\/\\/si0.twimg.com\\/images\\/themes\\/theme1\\/bg.png\",\"favourites_count\":0,\"follow_request_sent\":false,\"geo_enabled\":false,\"profile_background_color\":\"C0DEED\",\"profile_background_tile\":false,\"followers_count\":64,\"profile_image_url\":\"http:\\/\\/a1.twimg.com\\/profile_images\\/1163391423\\/me_normal.jpg\",\"screen_name\":\"carson_gross\",\"show_all_inline_media\":false,\"profile_sidebar_fill_color\":\"DDEEF6\",\"profile_image_url_https\":\"https:\\/\\/si0.twimg.com\\/profile_images\\/1163391423\\/me_normal.jpg\",\"listed_count\":6,\"notifications\":false,\"location\":\"Sacramento, California\",\"id\":213626025,\"verified\":false,\"time_zone\":\"Pacific Time (US & Canada)\",\"profile_sidebar_border_color\":\"C0DEED\"},\"in_reply_to_user_id\":null,\"retweet_count\":1,\"in_reply_to_screen_name\":null,\"in_reply_to_status_id_str\":null,\"retweeted\":false,\"source\":\"web\",\"in_reply_to_user_id_str\":null,\"coordinates\":null,\"id\":151446809196232704,\"text\":\"Well that went in pretty easily. Statically typed JSON-like support in Gosu:  \\u007B :People = \\u007B \\u007B :Name = \\\"Fred\\\" \\u007D, \\u007B :Name = \\\"Joe\\\" \\u007D \\u007D \\u007D #sweet\"},{\"id_str\":\"151428611067478016\",\"in_reply_to_status_id\":null,\"contributors\":null,\"place\":null,\"truncated\":false,\"geo\":null,\"favorited\":false,\"created_at\":\"Mon Dec 26 22:26:08 +0000 2011\",\"user\":{\"id_str\":\"213626025\",\"contributors_enabled\":false,\"lang\":\"en\",\"protected\":false,\"url\":\"http:\\/\\/calbear.org\",\"profile_use_background_image\":true,\"name\":\"Carson Gross\",\"default_profile_image\":false,\"friends_count\":14,\"profile_text_color\":\"333333\",\"statuses_count\":774,\"profile_background_image_url\":\"http:\\/\\/a0.twimg.com\\/images\\/themes\\/theme1\\/bg.png\",\"utc_offset\":-28800,\"description\":\"http:\\/\\/gosu-lang.org | http:\\/\\/ronin-web.org | http:\\/\\/vark.github.com ...\\nI like old diesel Land Cruisers.\",\"is_translator\":false,\"created_at\":\"Tue Nov 09 11:45:52 +0000 2010\",\"profile_link_color\":\"0084B4\",\"default_profile\":true,\"following\":true,\"profile_background_image_url_https\":\"https:\\/\\/si0.twimg.com\\/images\\/themes\\/theme1\\/bg.png\",\"favourites_count\":0,\"follow_request_sent\":false,\"geo_enabled\":false,\"profile_background_color\":\"C0DEED\",\"profile_background_tile\":false,\"followers_count\":64,\"profile_image_url\":\"http:\\/\\/a1.twimg.com\\/profile_images\\/1163391423\\/me_normal.jpg\",\"screen_name\":\"carson_gross\",\"show_all_inline_media\":false,\"profile_sidebar_fill_color\":\"DDEEF6\",\"profile_image_url_https\":\"https:\\/\\/si0.twimg.com\\/profile_images\\/1163391423\\/me_normal.jpg\",\"listed_count\":6,\"notifications\":false,\"location\":\"Sacramento, California\",\"id\":213626025,\"verified\":false,\"time_zone\":\"Pacific Time (US & Canada)\",\"profile_sidebar_border_color\":\"C0DEED\"},\"in_reply_to_user_id\":null,\"retweet_count\":0,\"in_reply_to_screen_name\":null,\"in_reply_to_status_id_str\":null,\"retweeted\":false,\"source\":\"web\",\"in_reply_to_user_id_str\":null,\"coordinates\":null,\"id\":151428611067478016,\"text\":\"OK, time to man up and implement raw gosu object literal support.\"},{\"id_str\":\"151324370801934336\",\"in_reply_to_status_id\":151182466428837888,\"contributors\":null,\"place\":null,\"truncated\":false,\"geo\":null,\"favorited\":false,\"created_at\":\"Mon Dec 26 15:31:56 +0000 2011\",\"user\":{\"id_str\":\"213626025\",\"contributors_enabled\":false,\"lang\":\"en\",\"protected\":false,\"url\":\"http:\\/\\/calbear.org\",\"profile_use_background_image\":true,\"name\":\"Carson Gross\",\"default_profile_image\":false,\"friends_count\":14,\"profile_text_color\":\"333333\",\"statuses_count\":774,\"profile_background_image_url\":\"http:\\/\\/a0.twimg.com\\/images\\/themes\\/theme1\\/bg.png\",\"utc_offset\":-28800,\"description\":\"http:\\/\\/gosu-lang.org | http:\\/\\/ronin-web.org | http:\\/\\/vark.github.com ...\\nI like old diesel Land Cruisers.\",\"is_translator\":false,\"created_at\":\"Tue Nov 09 11:45:52 +0000 2010\",\"profile_link_color\":\"0084B4\",\"following\":null,\"profile_background_image_url_https\":\"https:\\/\\/si0.twimg.com\\/images\\/themes\\/theme1\\/bg.png\",\"favourites_count\":0,\"follow_request_sent\":null,\"geo_enabled\":false,\"profile_background_color\":\"C0DEED\",\"profile_background_tile\":false,\"followers_count\":64,\"profile_image_url\":\"http:\\/\\/a1.twimg.com\\/profile_images\\/1163391423\\/me_normal.jpg\",\"screen_name\":\"carson_gross\",\"show_all_inline_media\":false,\"profile_sidebar_fill_color\":\"DDEEF6\",\"profile_image_url_https\":\"https:\\/\\/si0.twimg.com\\/profile_images\\/1163391423\\/me_normal.jpg\",\"listed_count\":6,\"notifications\":null,\"location\":\"Sacramento, California\",\"id\":213626025,\"default_profile\":true,\"verified\":false,\"time_zone\":\"Pacific Time (US & Canada)\",\"profile_sidebar_border_color\":\"C0DEED\"},\"in_reply_to_user_id\":76805362,\"retweet_count\":0,\"in_reply_to_screen_name\":\"am2605\",\"in_reply_to_status_id_str\":\"151182466428837888\",\"retweeted\":false,\"source\":\"web\",\"in_reply_to_user_id_str\":\"76805362\",\"coordinates\":null,\"id\":151324370801934336,\"text\":\"@am2605 Awesome!  On github?\"},{\"id_str\":\"151058199376691201\",\"in_reply_to_status_id\":null,\"contributors\":null,\"place\":null,\"truncated\":false,\"geo\":null,\"favorited\":false,\"created_at\":\"Sun Dec 25 21:54:15 +0000 2011\",\"user\":{\"id_str\":\"213626025\",\"contributors_enabled\":false,\"lang\":\"en\",\"protected\":false,\"url\":\"http:\\/\\/calbear.org\",\"profile_use_background_image\":true,\"name\":\"Carson Gross\",\"default_profile_image\":false,\"friends_count\":14,\"profile_text_color\":\"333333\",\"statuses_count\":774,\"profile_background_image_url\":\"http:\\/\\/a0.twimg.com\\/images\\/themes\\/theme1\\/bg.png\",\"utc_offset\":-28800,\"description\":\"http:\\/\\/gosu-lang.org | http:\\/\\/ronin-web.org | http:\\/\\/vark.github.com ...\\nI like old diesel Land Cruisers.\",\"is_translator\":false,\"created_at\":\"Tue Nov 09 11:45:52 +0000 2010\",\"profile_link_color\":\"0084B4\",\"default_profile\":true,\"following\":true,\"profile_background_image_url_https\":\"https:\\/\\/si0.twimg.com\\/images\\/themes\\/theme1\\/bg.png\",\"favourites_count\":0,\"follow_request_sent\":false,\"geo_enabled\":false,\"profile_background_color\":\"C0DEED\",\"profile_background_tile\":false,\"followers_count\":64,\"profile_image_url\":\"http:\\/\\/a1.twimg.com\\/profile_images\\/1163391423\\/me_normal.jpg\",\"screen_name\":\"carson_gross\",\"show_all_inline_media\":false,\"profile_sidebar_fill_color\":\"DDEEF6\",\"profile_image_url_https\":\"https:\\/\\/si0.twimg.com\\/profile_images\\/1163391423\\/me_normal.jpg\",\"listed_count\":6,\"notifications\":false,\"location\":\"Sacramento, California\",\"id\":213626025,\"verified\":false,\"time_zone\":\"Pacific Time (US & Canada)\",\"profile_sidebar_border_color\":\"C0DEED\"},\"in_reply_to_user_id\":null,\"retweet_count\":0,\"in_reply_to_screen_name\":null,\"in_reply_to_status_id_str\":null,\"retweeted\":false,\"source\":\"web\",\"in_reply_to_user_id_str\":null,\"coordinates\":null,\"id\":151058199376691201,\"text\":\"Happiness is 16 gigs of ram\"},{\"id_str\":\"151047724719022081\",\"in_reply_to_status_id\":150960724506722305,\"contributors\":null,\"place\":null,\"truncated\":false,\"geo\":null,\"favorited\":false,\"created_at\":\"Sun Dec 25 21:12:38 +0000 2011\",\"user\":{\"id_str\":\"213626025\",\"contributors_enabled\":false,\"lang\":\"en\",\"protected\":false,\"url\":\"http:\\/\\/calbear.org\",\"default_profile\":true,\"profile_use_background_image\":true,\"name\":\"Carson Gross\",\"default_profile_image\":false,\"friends_count\":14,\"profile_text_color\":\"333333\",\"statuses_count\":774,\"profile_background_image_url\":\"http:\\/\\/a0.twimg.com\\/images\\/themes\\/theme1\\/bg.png\",\"utc_offset\":-28800,\"description\":\"http:\\/\\/gosu-lang.org | http:\\/\\/ronin-web.org | http:\\/\\/vark.github.com ...\\nI like old diesel Land Cruisers.\",\"is_translator\":false,\"created_at\":\"Tue Nov 09 11:45:52 +0000 2010\",\"profile_link_color\":\"0084B4\",\"following\":null,\"profile_background_image_url_https\":\"https:\\/\\/si0.twimg.com\\/images\\/themes\\/theme1\\/bg.png\",\"favourites_count\":0,\"follow_request_sent\":null,\"geo_enabled\":false,\"profile_background_color\":\"C0DEED\",\"profile_background_tile\":false,\"followers_count\":64,\"profile_image_url\":\"http:\\/\\/a1.twimg.com\\/profile_images\\/1163391423\\/me_normal.jpg\",\"screen_name\":\"carson_gross\",\"show_all_inline_media\":false,\"profile_sidebar_fill_color\":\"DDEEF6\",\"profile_image_url_https\":\"https:\\/\\/si0.twimg.com\\/profile_images\\/1163391423\\/me_normal.jpg\",\"listed_count\":6,\"notifications\":null,\"location\":\"Sacramento, California\",\"id\":213626025,\"verified\":false,\"time_zone\":\"Pacific Time (US & Canada)\",\"profile_sidebar_border_color\":\"C0DEED\"},\"in_reply_to_user_id\":17188903,\"retweet_count\":0,\"in_reply_to_screen_name\":\"evanchooly\",\"in_reply_to_status_id_str\":\"150960724506722305\",\"retweeted\":false,\"source\":\"web\",\"in_reply_to_user_id_str\":\"17188903\",\"coordinates\":null,\"id\":151047724719022081,\"text\":\"@evanchooly So is there anyone u guys know @ Teh Goog to beat on about a browser VM spec? cc @headius @djspiewak @jsuereth @jamesiry @cbuest\"},{\"id_str\":\"150995742461411328\",\"in_reply_to_status_id\":150960724506722305,\"contributors\":null,\"place\":null,\"truncated\":false,\"geo\":null,\"favorited\":false,\"created_at\":\"Sun Dec 25 17:46:05 +0000 2011\",\"user\":{\"id_str\":\"213626025\",\"contributors_enabled\":false,\"lang\":\"en\",\"protected\":false,\"url\":\"http:\\/\\/calbear.org\",\"profile_use_background_image\":true,\"name\":\"Carson Gross\",\"default_profile_image\":false,\"friends_count\":14,\"profile_text_color\":\"333333\",\"statuses_count\":774,\"profile_background_image_url\":\"http:\\/\\/a0.twimg.com\\/images\\/themes\\/theme1\\/bg.png\",\"utc_offset\":-28800,\"description\":\"http:\\/\\/gosu-lang.org | http:\\/\\/ronin-web.org | http:\\/\\/vark.github.com ...\\nI like old diesel Land Cruisers.\",\"is_translator\":false,\"created_at\":\"Tue Nov 09 11:45:52 +0000 2010\",\"profile_link_color\":\"0084B4\",\"following\":null,\"profile_background_image_url_https\":\"https:\\/\\/si0.twimg.com\\/images\\/themes\\/theme1\\/bg.png\",\"favourites_count\":0,\"follow_request_sent\":null,\"geo_enabled\":false,\"profile_background_color\":\"C0DEED\",\"profile_background_tile\":false,\"followers_count\":64,\"profile_image_url\":\"http:\\/\\/a1.twimg.com\\/profile_images\\/1163391423\\/me_normal.jpg\",\"screen_name\":\"carson_gross\",\"show_all_inline_media\":false,\"profile_sidebar_fill_color\":\"DDEEF6\",\"profile_image_url_https\":\"https:\\/\\/si0.twimg.com\\/profile_images\\/1163391423\\/me_normal.jpg\",\"listed_count\":6,\"notifications\":null,\"location\":\"Sacramento, California\",\"id\":213626025,\"default_profile\":true,\"verified\":false,\"time_zone\":\"Pacific Time (US & Canada)\",\"profile_sidebar_border_color\":\"C0DEED\"},\"in_reply_to_user_id\":17188903,\"retweet_count\":0,\"in_reply_to_screen_name\":\"evanchooly\",\"in_reply_to_status_id_str\":\"150960724506722305\",\"retweeted\":false,\"source\":\"\\u003Ca href=\\\"http:\\/\\/twitter.com\\/#!\\/download\\/iphone\\\" rel=\\\"nofollow\\\"\\u003ETwitter for iPhone\\u003C\\/a\\u003E\",\"in_reply_to_user_id_str\":\"17188903\",\"coordinates\":null,\"id\":150995742461411328,\"text\":\"@evanchooly still, ill give it a try: node.js\"},{\"id_str\":\"150995634827173890\",\"in_reply_to_status_id\":150960724506722305,\"contributors\":null,\"place\":null,\"truncated\":false,\"geo\":null,\"favorited\":false,\"created_at\":\"Sun Dec 25 17:45:39 +0000 2011\",\"user\":{\"id_str\":\"213626025\",\"contributors_enabled\":false,\"lang\":\"en\",\"protected\":false,\"url\":\"http:\\/\\/calbear.org\",\"default_profile\":true,\"profile_use_background_image\":true,\"name\":\"Carson Gross\",\"default_profile_image\":false,\"friends_count\":14,\"profile_text_color\":\"333333\",\"statuses_count\":774,\"profile_background_image_url\":\"http:\\/\\/a0.twimg.com\\/images\\/themes\\/theme1\\/bg.png\",\"utc_offset\":-28800,\"description\":\"http:\\/\\/gosu-lang.org | http:\\/\\/ronin-web.org | http:\\/\\/vark.github.com ...\\nI like old diesel Land Cruisers.\",\"is_translator\":false,\"created_at\":\"Tue Nov 09 11:45:52 +0000 2010\",\"profile_link_color\":\"0084B4\",\"following\":null,\"profile_background_image_url_https\":\"https:\\/\\/si0.twimg.com\\/images\\/themes\\/theme1\\/bg.png\",\"favourites_count\":0,\"follow_request_sent\":null,\"geo_enabled\":false,\"profile_background_color\":\"C0DEED\",\"profile_background_tile\":false,\"followers_count\":64,\"profile_image_url\":\"http:\\/\\/a1.twimg.com\\/profile_images\\/1163391423\\/me_normal.jpg\",\"screen_name\":\"carson_gross\",\"show_all_inline_media\":false,\"profile_sidebar_fill_color\":\"DDEEF6\",\"profile_image_url_https\":\"https:\\/\\/si0.twimg.com\\/profile_images\\/1163391423\\/me_normal.jpg\",\"listed_count\":6,\"notifications\":null,\"location\":\"Sacramento, California\",\"id\":213626025,\"verified\":false,\"time_zone\":\"Pacific Time (US & Canada)\",\"profile_sidebar_border_color\":\"C0DEED\"},\"in_reply_to_user_id\":17188903,\"retweet_count\":0,\"in_reply_to_screen_name\":\"evanchooly\",\"in_reply_to_status_id_str\":\"150960724506722305\",\"retweeted\":false,\"source\":\"\\u003Ca href=\\\"http:\\/\\/twitter.com\\/#!\\/download\\/iphone\\\" rel=\\\"nofollow\\\"\\u003ETwitter for iPhone\\u003C\\/a\\u003E\",\"in_reply_to_user_id_str\":\"17188903\",\"coordinates\":null,\"id\":150995634827173890,\"text\":\"@evanchooly That's top 1 by so far its hard to see any others. Not because google was so stupid, but because the opportunity was\\/is so huge.\"},{\"id_str\":\"150432926822182915\",\"in_reply_to_status_id\":150344170064707584,\"contributors\":null,\"place\":null,\"truncated\":false,\"geo\":null,\"favorited\":false,\"created_at\":\"Sat Dec 24 04:29:39 +0000 2011\",\"user\":{\"id_str\":\"213626025\",\"contributors_enabled\":false,\"lang\":\"en\",\"protected\":false,\"url\":\"http:\\/\\/calbear.org\",\"profile_use_background_image\":true,\"name\":\"Carson Gross\",\"default_profile_image\":false,\"friends_count\":14,\"profile_text_color\":\"333333\",\"statuses_count\":774,\"profile_background_image_url\":\"http:\\/\\/a0.twimg.com\\/images\\/themes\\/theme1\\/bg.png\",\"utc_offset\":-28800,\"description\":\"http:\\/\\/gosu-lang.org | http:\\/\\/ronin-web.org | http:\\/\\/vark.github.com ...\\nI like old diesel Land Cruisers.\",\"is_translator\":false,\"created_at\":\"Tue Nov 09 11:45:52 +0000 2010\",\"profile_link_color\":\"0084B4\",\"following\":null,\"profile_background_image_url_https\":\"https:\\/\\/si0.twimg.com\\/images\\/themes\\/theme1\\/bg.png\",\"favourites_count\":0,\"follow_request_sent\":null,\"geo_enabled\":false,\"profile_background_color\":\"C0DEED\",\"profile_background_tile\":false,\"followers_count\":64,\"profile_image_url\":\"http:\\/\\/a1.twimg.com\\/profile_images\\/1163391423\\/me_normal.jpg\",\"screen_name\":\"carson_gross\",\"show_all_inline_media\":false,\"profile_sidebar_fill_color\":\"DDEEF6\",\"profile_image_url_https\":\"https:\\/\\/si0.twimg.com\\/profile_images\\/1163391423\\/me_normal.jpg\",\"listed_count\":6,\"notifications\":null,\"location\":\"Sacramento, California\",\"id\":213626025,\"default_profile\":true,\"verified\":false,\"time_zone\":\"Pacific Time (US & Canada)\",\"profile_sidebar_border_color\":\"C0DEED\"},\"in_reply_to_user_id\":76805362,\"retweet_count\":0,\"in_reply_to_screen_name\":\"am2605\",\"in_reply_to_status_id_str\":\"150344170064707584\",\"retweeted\":false,\"source\":\"web\",\"in_reply_to_user_id_str\":\"76805362\",\"coordinates\":null,\"id\":150432926822182915,\"text\":\"@am2605 Ha. I'm thinking \\\"Ok, Ok, I get it Big Guy: I need to be a better guy.  Do you have to rub my nose in it?\\\" Merry Christmas, man!\"},{\"id_str\":\"150430512362692609\",\"in_reply_to_status_id\":150375090259890176,\"contributors\":null,\"place\":null,\"truncated\":false,\"geo\":null,\"favorited\":false,\"created_at\":\"Sat Dec 24 04:20:03 +0000 2011\",\"user\":{\"id_str\":\"213626025\",\"default_profile\":true,\"contributors_enabled\":false,\"lang\":\"en\",\"protected\":false,\"url\":\"http:\\/\\/calbear.org\",\"profile_use_background_image\":true,\"name\":\"Carson Gross\",\"default_profile_image\":false,\"friends_count\":14,\"profile_text_color\":\"333333\",\"statuses_count\":774,\"profile_background_image_url\":\"http:\\/\\/a0.twimg.com\\/images\\/themes\\/theme1\\/bg.png\",\"utc_offset\":-28800,\"description\":\"http:\\/\\/gosu-lang.org | http:\\/\\/ronin-web.org | http:\\/\\/vark.github.com ...\\nI like old diesel Land Cruisers.\",\"is_translator\":false,\"created_at\":\"Tue Nov 09 11:45:52 +0000 2010\",\"profile_link_color\":\"0084B4\",\"following\":false,\"profile_background_image_url_https\":\"https:\\/\\/si0.twimg.com\\/images\\/themes\\/theme1\\/bg.png\",\"favourites_count\":0,\"follow_request_sent\":false,\"geo_enabled\":false,\"profile_background_color\":\"C0DEED\",\"profile_background_tile\":false,\"followers_count\":64,\"profile_image_url\":\"http:\\/\\/a1.twimg.com\\/profile_images\\/1163391423\\/me_normal.jpg\",\"screen_name\":\"carson_gross\",\"show_all_inline_media\":false,\"profile_sidebar_fill_color\":\"DDEEF6\",\"profile_image_url_https\":\"https:\\/\\/si0.twimg.com\\/profile_images\\/1163391423\\/me_normal.jpg\",\"listed_count\":6,\"notifications\":false,\"location\":\"Sacramento, California\",\"id\":213626025,\"verified\":false,\"time_zone\":\"Pacific Time (US & Canada)\",\"profile_sidebar_border_color\":\"C0DEED\"},\"in_reply_to_user_id\":19044984,\"retweet_count\":0,\"in_reply_to_screen_name\":\"jamesiry\",\"in_reply_to_status_id_str\":\"150375090259890176\",\"possibly_sensitive\":false,\"retweeted\":false,\"source\":\"web\",\"in_reply_to_user_id_str\":\"19044984\",\"coordinates\":null,\"id\":150430512362692609,\"text\":\"@jamesiry @jsuereth @headius Sure.  Still, w\\/o managed mem, it'd be painful.  Here's my wish list: https:\\/\\/t.co\\/YQAktBqn (*any* VM &gt; none)\"},{\"id_str\":\"150349153115975680\",\"in_reply_to_status_id\":null,\"contributors\":null,\"place\":null,\"truncated\":false,\"geo\":null,\"favorited\":false,\"created_at\":\"Fri Dec 23 22:56:46 +0000 2011\",\"user\":{\"id_str\":\"213626025\",\"contributors_enabled\":false,\"lang\":\"en\",\"protected\":false,\"url\":\"http:\\/\\/calbear.org\",\"default_profile\":true,\"profile_use_background_image\":true,\"name\":\"Carson Gross\",\"default_profile_image\":false,\"friends_count\":14,\"profile_text_color\":\"333333\",\"statuses_count\":774,\"profile_background_image_url\":\"http:\\/\\/a0.twimg.com\\/images\\/themes\\/theme1\\/bg.png\",\"utc_offset\":-28800,\"description\":\"http:\\/\\/gosu-lang.org | http:\\/\\/ronin-web.org | http:\\/\\/vark.github.com ...\\nI like old diesel Land Cruisers.\",\"is_translator\":false,\"created_at\":\"Tue Nov 09 11:45:52 +0000 2010\",\"profile_link_color\":\"0084B4\",\"following\":null,\"profile_background_image_url_https\":\"https:\\/\\/si0.twimg.com\\/images\\/themes\\/theme1\\/bg.png\",\"favourites_count\":0,\"follow_request_sent\":null,\"geo_enabled\":false,\"profile_background_color\":\"C0DEED\",\"profile_background_tile\":false,\"followers_count\":64,\"profile_image_url\":\"http:\\/\\/a1.twimg.com\\/profile_images\\/1163391423\\/me_normal.jpg\",\"screen_name\":\"carson_gross\",\"show_all_inline_media\":false,\"profile_sidebar_fill_color\":\"DDEEF6\",\"profile_image_url_https\":\"https:\\/\\/si0.twimg.com\\/profile_images\\/1163391423\\/me_normal.jpg\",\"listed_count\":6,\"notifications\":null,\"location\":\"Sacramento, California\",\"id\":213626025,\"verified\":false,\"time_zone\":\"Pacific Time (US & Canada)\",\"profile_sidebar_border_color\":\"C0DEED\"},\"in_reply_to_user_id\":9989362,\"retweet_count\":0,\"in_reply_to_screen_name\":\"headius\",\"in_reply_to_status_id_str\":null,\"retweeted\":false,\"source\":\"web\",\"in_reply_to_user_id_str\":\"9989362\",\"coordinates\":null,\"id\":150349153115975680,\"text\":\"@headius @jsuereth I should note, my notion of a sane vm spec includes a map and list primitive.  Some sanes are saner than others. ;)\"},{\"id_str\":\"150348206843240448\",\"in_reply_to_status_id\":150347808598274048,\"contributors\":null,\"place\":null,\"truncated\":false,\"geo\":null,\"favorited\":false,\"created_at\":\"Fri Dec 23 22:53:00 +0000 2011\",\"user\":{\"id_str\":\"213626025\",\"contributors_enabled\":false,\"lang\":\"en\",\"protected\":false,\"url\":\"http:\\/\\/calbear.org\",\"profile_use_background_image\":true,\"name\":\"Carson Gross\",\"default_profile_image\":false,\"friends_count\":14,\"profile_text_color\":\"333333\",\"statuses_count\":774,\"profile_background_image_url\":\"http:\\/\\/a0.twimg.com\\/images\\/themes\\/theme1\\/bg.png\",\"utc_offset\":-28800,\"description\":\"http:\\/\\/gosu-lang.org | http:\\/\\/ronin-web.org | http:\\/\\/vark.github.com ...\\nI like old diesel Land Cruisers.\",\"is_translator\":false,\"created_at\":\"Tue Nov 09 11:45:52 +0000 2010\",\"profile_link_color\":\"0084B4\",\"following\":null,\"profile_background_image_url_https\":\"https:\\/\\/si0.twimg.com\\/images\\/themes\\/theme1\\/bg.png\",\"favourites_count\":0,\"follow_request_sent\":null,\"geo_enabled\":false,\"profile_background_color\":\"C0DEED\",\"profile_background_tile\":false,\"followers_count\":64,\"profile_image_url\":\"http:\\/\\/a1.twimg.com\\/profile_images\\/1163391423\\/me_normal.jpg\",\"screen_name\":\"carson_gross\",\"show_all_inline_media\":false,\"profile_sidebar_fill_color\":\"DDEEF6\",\"profile_image_url_https\":\"https:\\/\\/si0.twimg.com\\/profile_images\\/1163391423\\/me_normal.jpg\",\"listed_count\":6,\"notifications\":null,\"location\":\"Sacramento, California\",\"id\":213626025,\"default_profile\":true,\"verified\":false,\"time_zone\":\"Pacific Time (US & Canada)\",\"profile_sidebar_border_color\":\"C0DEED\"},\"in_reply_to_user_id\":9989362,\"retweet_count\":0,\"in_reply_to_screen_name\":\"headius\",\"in_reply_to_status_id_str\":\"150347808598274048\",\"retweeted\":false,\"source\":\"web\",\"in_reply_to_user_id_str\":\"9989362\",\"coordinates\":null,\"id\":150348206843240448,\"text\":\"@headius @jsuereth Not a crazy idea, but you don't get the tools advantage: client side debugging, sane stack traces, etc.\"},{\"id_str\":\"150347869231120384\",\"in_reply_to_status_id\":150346597660762113,\"contributors\":null,\"place\":null,\"truncated\":false,\"geo\":null,\"favorited\":false,\"created_at\":\"Fri Dec 23 22:51:40 +0000 2011\",\"user\":{\"id_str\":\"213626025\",\"contributors_enabled\":false,\"lang\":\"en\",\"protected\":false,\"url\":\"http:\\/\\/calbear.org\",\"default_profile\":true,\"profile_use_background_image\":true,\"name\":\"Carson Gross\",\"default_profile_image\":false,\"friends_count\":14,\"profile_text_color\":\"333333\",\"statuses_count\":774,\"profile_background_image_url\":\"http:\\/\\/a0.twimg.com\\/images\\/themes\\/theme1\\/bg.png\",\"utc_offset\":-28800,\"description\":\"http:\\/\\/gosu-lang.org | http:\\/\\/ronin-web.org | http:\\/\\/vark.github.com ...\\nI like old diesel Land Cruisers.\",\"is_translator\":false,\"created_at\":\"Tue Nov 09 11:45:52 +0000 2010\",\"profile_link_color\":\"0084B4\",\"following\":null,\"profile_background_image_url_https\":\"https:\\/\\/si0.twimg.com\\/images\\/themes\\/theme1\\/bg.png\",\"favourites_count\":0,\"follow_request_sent\":null,\"geo_enabled\":false,\"profile_background_color\":\"C0DEED\",\"profile_background_tile\":false,\"followers_count\":64,\"profile_image_url\":\"http:\\/\\/a1.twimg.com\\/profile_images\\/1163391423\\/me_normal.jpg\",\"screen_name\":\"carson_gross\",\"show_all_inline_media\":false,\"profile_sidebar_fill_color\":\"DDEEF6\",\"profile_image_url_https\":\"https:\\/\\/si0.twimg.com\\/profile_images\\/1163391423\\/me_normal.jpg\",\"listed_count\":6,\"notifications\":null,\"location\":\"Sacramento, California\",\"id\":213626025,\"verified\":false,\"time_zone\":\"Pacific Time (US & Canada)\",\"profile_sidebar_border_color\":\"C0DEED\"},\"in_reply_to_user_id\":9989362,\"retweet_count\":0,\"in_reply_to_screen_name\":\"headius\",\"in_reply_to_status_id_str\":\"150346597660762113\",\"retweeted\":false,\"source\":\"web\",\"in_reply_to_user_id_str\":\"9989362\",\"coordinates\":null,\"id\":150347869231120384,\"text\":\"@headius @jsuereth Ha! To be clear, I consider automatic GC part of a 'sane' VM, so I guess raw LLVM is out. You know what I mean: sane ;)\"},{\"id_str\":\"150347323963211777\",\"in_reply_to_status_id\":150345459955806209,\"contributors\":null,\"place\":null,\"truncated\":false,\"geo\":null,\"favorited\":false,\"created_at\":\"Fri Dec 23 22:49:30 +0000 2011\",\"user\":{\"id_str\":\"213626025\",\"contributors_enabled\":false,\"lang\":\"en\",\"protected\":false,\"url\":\"http:\\/\\/calbear.org\",\"profile_use_background_image\":true,\"name\":\"Carson Gross\",\"default_profile_image\":false,\"friends_count\":14,\"profile_text_color\":\"333333\",\"statuses_count\":774,\"profile_background_image_url\":\"http:\\/\\/a0.twimg.com\\/images\\/themes\\/theme1\\/bg.png\",\"utc_offset\":-28800,\"description\":\"http:\\/\\/gosu-lang.org | http:\\/\\/ronin-web.org | http:\\/\\/vark.github.com ...\\nI like old diesel Land Cruisers.\",\"is_translator\":false,\"created_at\":\"Tue Nov 09 11:45:52 +0000 2010\",\"profile_link_color\":\"0084B4\",\"following\":null,\"profile_background_image_url_https\":\"https:\\/\\/si0.twimg.com\\/images\\/themes\\/theme1\\/bg.png\",\"favourites_count\":0,\"default_profile\":true,\"follow_request_sent\":null,\"geo_enabled\":false,\"profile_background_color\":\"C0DEED\",\"profile_background_tile\":false,\"followers_count\":64,\"profile_image_url\":\"http:\\/\\/a1.twimg.com\\/profile_images\\/1163391423\\/me_normal.jpg\",\"screen_name\":\"carson_gross\",\"show_all_inline_media\":false,\"profile_sidebar_fill_color\":\"DDEEF6\",\"profile_image_url_https\":\"https:\\/\\/si0.twimg.com\\/profile_images\\/1163391423\\/me_normal.jpg\",\"listed_count\":6,\"notifications\":null,\"location\":\"Sacramento, California\",\"id\":213626025,\"verified\":false,\"time_zone\":\"Pacific Time (US & Canada)\",\"profile_sidebar_border_color\":\"C0DEED\"},\"in_reply_to_user_id\":19418890,\"retweet_count\":0,\"in_reply_to_screen_name\":\"jsuereth\",\"in_reply_to_status_id_str\":\"150345459955806209\",\"retweeted\":false,\"source\":\"web\",\"in_reply_to_user_id_str\":\"19418890\",\"coordinates\":null,\"id\":150347323963211777,\"text\":\"@jsuereth @headius Im agnostic\\/indifferent towards the implementation so long as it is sane and open. Far more important is that it happens.\"},{\"id_str\":\"150343443724509185\",\"in_reply_to_status_id\":150339497572315136,\"contributors\":null,\"place\":null,\"truncated\":false,\"geo\":null,\"favorited\":false,\"created_at\":\"Fri Dec 23 22:34:04 +0000 2011\",\"user\":{\"id_str\":\"213626025\",\"contributors_enabled\":false,\"lang\":\"en\",\"protected\":false,\"url\":\"http:\\/\\/calbear.org\",\"default_profile\":true,\"profile_use_background_image\":true,\"name\":\"Carson Gross\",\"default_profile_image\":false,\"friends_count\":14,\"profile_text_color\":\"333333\",\"statuses_count\":774,\"profile_background_image_url\":\"http:\\/\\/a0.twimg.com\\/images\\/themes\\/theme1\\/bg.png\",\"utc_offset\":-28800,\"description\":\"http:\\/\\/gosu-lang.org | http:\\/\\/ronin-web.org | http:\\/\\/vark.github.com ...\\nI like old diesel Land Cruisers.\",\"is_translator\":false,\"created_at\":\"Tue Nov 09 11:45:52 +0000 2010\",\"profile_link_color\":\"0084B4\",\"following\":null,\"profile_background_image_url_https\":\"https:\\/\\/si0.twimg.com\\/images\\/themes\\/theme1\\/bg.png\",\"favourites_count\":0,\"follow_request_sent\":null,\"geo_enabled\":false,\"profile_background_color\":\"C0DEED\",\"profile_background_tile\":false,\"followers_count\":64,\"profile_image_url\":\"http:\\/\\/a1.twimg.com\\/profile_images\\/1163391423\\/me_normal.jpg\",\"screen_name\":\"carson_gross\",\"show_all_inline_media\":false,\"profile_sidebar_fill_color\":\"DDEEF6\",\"profile_image_url_https\":\"https:\\/\\/si0.twimg.com\\/profile_images\\/1163391423\\/me_normal.jpg\",\"listed_count\":6,\"notifications\":null,\"location\":\"Sacramento, California\",\"id\":213626025,\"verified\":false,\"time_zone\":\"Pacific Time (US & Canada)\",\"profile_sidebar_border_color\":\"C0DEED\"},\"in_reply_to_user_id\":19418890,\"retweet_count\":0,\"in_reply_to_screen_name\":\"jsuereth\",\"in_reply_to_status_id_str\":\"150339497572315136\",\"retweeted\":false,\"source\":\"web\",\"in_reply_to_user_id_str\":\"19418890\",\"coordinates\":null,\"id\":150343443724509185,\"text\":\"@jsuereth @headius I mean web 3.0 byte-code. The JVM spec might provide some inspiration. The explosion of creativity\\/play would be awesome.\"},{\"id_str\":\"150338560392835072\",\"in_reply_to_status_id\":null,\"contributors\":null,\"place\":null,\"truncated\":false,\"geo\":null,\"favorited\":false,\"created_at\":\"Fri Dec 23 22:14:40 +0000 2011\",\"user\":{\"id_str\":\"213626025\",\"contributors_enabled\":false,\"lang\":\"en\",\"protected\":false,\"url\":\"http:\\/\\/calbear.org\",\"default_profile\":true,\"profile_use_background_image\":true,\"name\":\"Carson Gross\",\"default_profile_image\":false,\"friends_count\":14,\"profile_text_color\":\"333333\",\"statuses_count\":774,\"profile_background_image_url\":\"http:\\/\\/a0.twimg.com\\/images\\/themes\\/theme1\\/bg.png\",\"utc_offset\":-28800,\"description\":\"http:\\/\\/gosu-lang.org | http:\\/\\/ronin-web.org | http:\\/\\/vark.github.com ...\\nI like old diesel Land Cruisers.\",\"is_translator\":false,\"created_at\":\"Tue Nov 09 11:45:52 +0000 2010\",\"profile_link_color\":\"0084B4\",\"following\":null,\"profile_background_image_url_https\":\"https:\\/\\/si0.twimg.com\\/images\\/themes\\/theme1\\/bg.png\",\"favourites_count\":0,\"follow_request_sent\":null,\"geo_enabled\":false,\"profile_background_color\":\"C0DEED\",\"profile_background_tile\":false,\"followers_count\":64,\"profile_image_url\":\"http:\\/\\/a1.twimg.com\\/profile_images\\/1163391423\\/me_normal.jpg\",\"screen_name\":\"carson_gross\",\"show_all_inline_media\":false,\"profile_sidebar_fill_color\":\"DDEEF6\",\"profile_image_url_https\":\"https:\\/\\/si0.twimg.com\\/profile_images\\/1163391423\\/me_normal.jpg\",\"listed_count\":6,\"notifications\":null,\"location\":\"Sacramento, California\",\"id\":213626025,\"verified\":false,\"time_zone\":\"Pacific Time (US & Canada)\",\"profile_sidebar_border_color\":\"C0DEED\"},\"in_reply_to_user_id\":null,\"retweet_count\":0,\"in_reply_to_screen_name\":null,\"in_reply_to_status_id_str\":null,\"retweeted\":false,\"source\":\"web\",\"in_reply_to_user_id_str\":null,\"coordinates\":null,\"id\":150338560392835072,\"text\":\"God really is hilarious: I have spent the last 24 hours writing a chrome plugin in javascript and an outlook plugin in visual basic.\"},{\"id_str\":\"150338137099481089\",\"in_reply_to_status_id\":150310899054415872,\"contributors\":null,\"place\":null,\"truncated\":false,\"geo\":null,\"favorited\":false,\"created_at\":\"Fri Dec 23 22:12:59 +0000 2011\",\"user\":{\"id_str\":\"213626025\",\"contributors_enabled\":false,\"lang\":\"en\",\"protected\":false,\"url\":\"http:\\/\\/calbear.org\",\"profile_use_background_image\":true,\"name\":\"Carson Gross\",\"default_profile_image\":false,\"friends_count\":14,\"profile_text_color\":\"333333\",\"statuses_count\":774,\"profile_background_image_url\":\"http:\\/\\/a0.twimg.com\\/images\\/themes\\/theme1\\/bg.png\",\"utc_offset\":-28800,\"description\":\"http:\\/\\/gosu-lang.org | http:\\/\\/ronin-web.org | http:\\/\\/vark.github.com ...\\nI like old diesel Land Cruisers.\",\"is_translator\":false,\"created_at\":\"Tue Nov 09 11:45:52 +0000 2010\",\"profile_link_color\":\"0084B4\",\"following\":null,\"profile_background_image_url_https\":\"https:\\/\\/si0.twimg.com\\/images\\/themes\\/theme1\\/bg.png\",\"favourites_count\":0,\"follow_request_sent\":null,\"geo_enabled\":false,\"profile_background_color\":\"C0DEED\",\"profile_background_tile\":false,\"followers_count\":64,\"profile_image_url\":\"http:\\/\\/a1.twimg.com\\/profile_images\\/1163391423\\/me_normal.jpg\",\"screen_name\":\"carson_gross\",\"show_all_inline_media\":false,\"profile_sidebar_fill_color\":\"DDEEF6\",\"profile_image_url_https\":\"https:\\/\\/si0.twimg.com\\/profile_images\\/1163391423\\/me_normal.jpg\",\"listed_count\":6,\"notifications\":null,\"location\":\"Sacramento, California\",\"id\":213626025,\"default_profile\":true,\"verified\":false,\"time_zone\":\"Pacific Time (US & Canada)\",\"profile_sidebar_border_color\":\"C0DEED\"},\"in_reply_to_user_id\":240111217,\"retweet_count\":0,\"in_reply_to_screen_name\":\"satchmigo\",\"in_reply_to_status_id_str\":\"150310899054415872\",\"retweeted\":false,\"source\":\"web\",\"in_reply_to_user_id_str\":\"240111217\",\"coordinates\":null,\"id\":150338137099481089,\"text\":\"@satchmigo I've got my dead lift over my bench for the first time ever.  The menu for Sunday's Christmas dinner?  PAIN.\"},{\"id_str\":\"150337582193061889\",\"in_reply_to_status_id\":150297353725816832,\"contributors\":null,\"place\":null,\"truncated\":false,\"geo\":null,\"favorited\":false,\"created_at\":\"Fri Dec 23 22:10:47 +0000 2011\",\"user\":{\"id_str\":\"213626025\",\"contributors_enabled\":false,\"lang\":\"en\",\"protected\":false,\"url\":\"http:\\/\\/calbear.org\",\"default_profile\":true,\"profile_use_background_image\":true,\"name\":\"Carson Gross\",\"default_profile_image\":false,\"friends_count\":14,\"profile_text_color\":\"333333\",\"statuses_count\":774,\"profile_background_image_url\":\"http:\\/\\/a0.twimg.com\\/images\\/themes\\/theme1\\/bg.png\",\"utc_offset\":-28800,\"description\":\"http:\\/\\/gosu-lang.org | http:\\/\\/ronin-web.org | http:\\/\\/vark.github.com ...\\nI like old diesel Land Cruisers.\",\"is_translator\":false,\"created_at\":\"Tue Nov 09 11:45:52 +0000 2010\",\"profile_link_color\":\"0084B4\",\"following\":null,\"profile_background_image_url_https\":\"https:\\/\\/si0.twimg.com\\/images\\/themes\\/theme1\\/bg.png\",\"favourites_count\":0,\"follow_request_sent\":null,\"geo_enabled\":false,\"profile_background_color\":\"C0DEED\",\"profile_background_tile\":false,\"followers_count\":64,\"profile_image_url\":\"http:\\/\\/a1.twimg.com\\/profile_images\\/1163391423\\/me_normal.jpg\",\"screen_name\":\"carson_gross\",\"show_all_inline_media\":false,\"profile_sidebar_fill_color\":\"DDEEF6\",\"profile_image_url_https\":\"https:\\/\\/si0.twimg.com\\/profile_images\\/1163391423\\/me_normal.jpg\",\"listed_count\":6,\"notifications\":null,\"location\":\"Sacramento, California\",\"id\":213626025,\"verified\":false,\"time_zone\":\"Pacific Time (US & Canada)\",\"profile_sidebar_border_color\":\"C0DEED\"},\"in_reply_to_user_id\":6586332,\"retweet_count\":0,\"in_reply_to_screen_name\":\"djspiewak\",\"in_reply_to_status_id_str\":\"150297353725816832\",\"retweeted\":false,\"source\":\"web\",\"in_reply_to_user_id_str\":\"6586332\",\"coordinates\":null,\"id\":150337582193061889,\"text\":\"@djspiewak And, with that, God bless us every one.\"}]");
  }

}
