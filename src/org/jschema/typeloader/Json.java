package org.jschema.typeloader;

import java.util.HashSet;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.Map;
import java.util.HashMap;
import java.math.*;

import java.util.Date;
import java.math.BigInteger;
import java.math.BigDecimal;

import gw.lang.reflect.IType;
import gw.lang.reflect.IPropertyInfo;
import gw.lang.reflect.TypeSystem;
import gw.lang.reflect.gs.IGosuObject;
import gw.lang.reflect.java.IJavaType;
import gw.lang.reflect.IEnumType;
import gw.lang.reflect.IEnumValue;

import org.jschema.parser.JSONParser;

public class Json implements IGosuObject {
  private Map json;
  private IType type;

  public Json(IType type) {
    this.type = type;
    this.json = new HashMap();
  }

  public Json(Object json, IType type) {
    if ((json instanceof Map) == false) {
      throw new JSONParserException("Must be a Map");
    }
    this.type = type;
    this.json = (Map)json;
  }

  /**
  * Creates Json object, validating structure of the json 
  * string against the JsonTypeInfo provided.
  */
  public Json(String json, JsonTypeInfo structure) {
    this.type = structure.getOwnersType();
    this.json = createJson((Map)JSONParser.parseJSON(json), structure);
  }

  //TODO add validity checks to verify the type is correct before parsing it.
  private Map createJson(Map it, JsonTypeInfo structure) {
    for (IPropertyInfo info : structure.getProperties()) {
      IType featureType = info.getFeatureType();
      String jsonName = structure.getJsonPropertyName(info.getName());
      if (!it.containsKey(jsonName)) {
        continue;
      }
      Object o = it.get(jsonName);
      it.remove(jsonName);

      Parse parse = PARSE.get(featureType);
      if (parse != null) {
        it.put(jsonName, parse.parse(o));
      //Hackadoodledoo to determine if it's a custom json type
      } else if (!(featureType instanceof IJavaType) && !(featureType instanceof gw.internal.gosu.parser.IJavaTypeInternal)) {
        if (o instanceof String) { //JsonEnumType
          it.put(jsonName, o);
        } else {
          it.put(jsonName, new Json(createJson((Map)o, (JsonTypeInfo)featureType.getTypeInfo()), featureType));
        }
      } else if (featureType instanceof gw.internal.gosu.parser.IJavaTypeInternal) { //hack to determine list
        if (o instanceof List) {
          List arr = (List)o;
          if (arr.size() == 0) {
            continue;
          }
          IType parameterizedType = featureType.getTypeParameters()[0];
          ArrayList rawList = createList(arr, parameterizedType);
          it.put(jsonName, rawList);
        } else if (o instanceof Map) {
          Map jsonObj = (Map)o;
          Parse keyParser = PARSE.get(featureType.getTypeParameters()[0]);
          Parse valueParser = PARSE.get(featureType.getTypeParameters()[1]);
          if (keyParser == null) {
            throw new RuntimeException("Map key currently only supports simple types that conform to simple JSON");
          }

          Map jsonMap = new HashMap();
          Set iterate = jsonObj.keySet();
          for (Object iter : iterate) {
            String itKey = (String)iter;
            if (valueParser == null) {
              jsonMap.put(keyParser.parse(itKey), 
                new Json(createJson((Map)jsonObj.get(itKey), 
                        (JsonTypeInfo)featureType.getTypeParameters()[1].getTypeInfo()),
                        featureType.getTypeParameters()[1]));
            } else {
              jsonMap.put(keyParser.parse(itKey), valueParser.parse(jsonObj.get(itKey)));
            }
          }
          
/*          while (iterate.hasNext()) {
            String itKey = (String)iterate.next();
            if (valueParser == null) {
              jsonMap.put(keyParser.parse(itKey), 
                new Json(createJson((JSONObject)jsonObj.get(itKey), 
                        (JsonTypeInfo)featureType.getTypeParameters()[1].getTypeInfo()),
                        featureType.getTypeParameters()[1]));
            } else {
              jsonMap.put(keyParser.parse(itKey), valueParser.parse(jsonObj.get(itKey)));
            }
          }*/
          it.put(jsonName, (Object)jsonMap);
        }
      }
    }
    return it;
  }

  private ArrayList createList(List arr, IType type) {
    ArrayList list = new ArrayList();
    for (int i = 0; i < arr.size(); i++) {
      Parse parser = PARSE.get(type);
      if (parser == null) {
        if (arr.get(i) instanceof String) { //enum
          IEnumType enumType = (IEnumType)type;
          list.add(enumType.getEnumValue(JsonEnumType.enumify((String)arr.get(i))));
          continue;
        } else {
          list.add(new Json(createJson((Map)arr.get(i), 
                  (JsonTypeInfo)type.getTypeInfo()), type));
          continue;
        }
      }
      list.add(parser.parse(arr.get(i)));
    }
    return list;
  }

  public Json(Object json) {
    if ((json instanceof Map) == false) {
      throw new JSONParserException("Must be a Map");
    }
    this.json = (Map)json;
  }

  public String serialize(int indentation) {
    Map output = serializeAsJSONObject();
/*    try {*/
    if (indentation == -1) {
      return JSONParser.serializeJSON(output);
    }
    return JSONParser.serializeJSON(output);
/*    } catch (JSONException e) {
      throw new RuntimeException(e);
    }*/
  }

  private Map serializeAsJSONObject() {
    Map output = new HashMap();

/*    try {*/
    for (String key : keys()) {
      JsonName name = new JsonName(key);
      Object value = get(key);

      //enum
      if (value instanceof JsonEnumType.JsonEnumValue) {
        JsonEnumType.JsonEnumValue enumVal = (JsonEnumType.JsonEnumValue)value;
        output.put(name.getJsonName(), enumVal.getJsonCode());
      //list
      } else if (value instanceof List) {
        List list = (List)value;
        List array = new ArrayList();
        if (output.containsKey(name.getJsonName())) {
          array = (List)output.get(name.getJsonName());
        } else {
          output.put(name.getJsonName(), array);
        }

        if (list.size() > 0) {

        }
        if (list.size() > 0 && list.get(0) instanceof Json) {
          List<Json> jsons = (List<Json>)list;
          for (Json j : jsons) {
            array.add(j.serializeAsJSONObject());
          }
        } else {
          for (Object o : list) {
            if (o instanceof Boolean || o instanceof String || o instanceof java.lang.Integer || o instanceof java.lang.Double) {
              array.add(o);
            } else if (o instanceof java.math.BigDecimal) {
              array.add(((BigDecimal)o).doubleValue()); //TODO loss of precision
            } else if (o instanceof java.math.BigInteger) {
              array.add(((BigInteger)o).longValue());
            } else if (o instanceof java.util.Date) {
              array.add(o.toString());
            } else if (o instanceof JsonEnumType.JsonEnumValue) {
              JsonEnumType.JsonEnumValue enumVal = (JsonEnumType.JsonEnumValue)o;
              array.add(enumVal.getJsonCode());
            }
          }
        }
        //json
        } else if (value instanceof Json) {
          Json current = (Json)value;//get(name.getJsonName());
          output.put(name.getJsonName(), current.serializeAsJSONObject());
        //jsonarray	
        //} else if (value instanceof JSONArray) {
/*            JSONArray arr = (JSONArray)value;
          for (int i = 0; i < arr.length(); i++) {
            Object o = arr.get(i);
            if (o instanceof Json) {
              arr.put(i, ((Json)o).serializeAsJSONObject());
            } else if (o instanceof Boolean || o instanceof String || o instanceof java.lang.Integer || o instanceof java.lang.Double) {
              arr.put(i, o);
            } else if (o instanceof java.math.BigDecimal) {
              arr.put(i, ((BigDecimal)o).doubleValue()); //TODO loss of precision
            } else if (o instanceof java.math.BigInteger) {
              arr.put(i, ((BigInteger)o).longValue());
            } else if (o instanceof java.util.Date) {
              arr.put(i, o.toString());
            }
          }*/

        //map 
        } else if (value instanceof java.util.Map) {
          handleJavaMapType(output, name.getJsonName(), (Map)value);
        //java types
        } else {
          handleJavaSimpleType(output, name.getJsonName(), value);
        } 
    }
/*    } catch (JSONException e) {
      throw new RuntimeException(e);
    }*/
    return output;
  }

  private void handleJavaMapType(Map output, String key, Map value) {
    Map j = new HashMap();
    for (Object mapKey : ((Map)value).keySet()) {
      Object get = ((Map)value).get(mapKey);
      if (get instanceof Json) {
        if (!output.containsKey(key)) {
          output.put(key, new HashMap());
        }
        Map mapOutput = (Map)output.get(key);
        mapOutput.put(mapKey.toString(), ((Json)get).serializeAsJSONObject());
      } else {
        handleJavaSimpleType(j, mapKey.toString(), get);
      }
    }
    if (!output.containsKey(key)) {
      output.put(key, j);
    }
  }

  private void handleJavaSimpleType(Map output, String key, Object value) {
    if (value instanceof Boolean || value instanceof String || value instanceof java.lang.Integer || value instanceof java.lang.Double) {
      output.put(key, value);
    } else if (value instanceof java.math.BigDecimal) {
      output.put(key, ((BigDecimal)value).doubleValue());
    } else if (value instanceof java.math.BigInteger) {
      output.put(key, ((BigInteger)value).longValue());
    } else if (value instanceof java.util.Date) {
      output.put(key, value.toString());
    }
  }

  public Object get(String key) {
    if (json.containsKey(key) == false) {
      return null;
    }
    return json.get(key);
  }

  public void put(String key, Object value) {
    json.put(key, value);
  }

  public Json getJson(String key) {
    return new Json(json.get(key), type);
  }

  public String toString() {
    String str = "[";
    for (String key : keys()) {
      str += key + ",";
    }
    str += "]";
    return str;
  }

  public Iterable<String> keys() {
    return (Iterable<String>)json.keySet();
  }

  @Override
  public IType getIntrinsicType() {
    return type;
  }
  
  private static interface Parse<T> {
    T parse(Object parsable);
  }

  private static final Map<IType, Parse> PARSE = new HashMap<IType, Parse>();
  static {
    PARSE.put(IJavaType.INTEGER, new Parse<Integer>() {
      public Integer parse(Object parse) {
        return Integer.valueOf(parse.toString());
      }
    });
    PARSE.put(IJavaType.DOUBLE, new Parse<Double>() {
      public Double parse(Object parse) {
        return Double.valueOf(parse.toString());
      }
    });
    PARSE.put(IJavaType.BIGDECIMAL, new Parse<BigDecimal>() {
      public BigDecimal parse(Object parse) {
        return new BigDecimal(parse instanceof Long ? Long.valueOf(parse.toString()) :
                              Double.valueOf(parse.toString()));
      }
    });
    PARSE.put(IJavaType.BIGINTEGER, new Parse<BigInteger>() {
      public BigInteger parse(Object parse) {
        return BigInteger.valueOf(Long.valueOf(parse.toString()));
      }
    });
    PARSE.put(IJavaType.STRING, new Parse<String>() {
      public String parse(Object parse) {
        return parse.toString();
      }
    });
    PARSE.put(IJavaType.BOOLEAN, new Parse<Boolean>() {
      public Boolean parse(Object parse) {
        return Boolean.valueOf(parse.toString());
      }
    });
    PARSE.put(IJavaType.DATE, new Parse<Date>() {
      public Date parse(Object parse) {
        return new Date(parse.toString());
      }
    });
  }
}