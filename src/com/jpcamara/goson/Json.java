package com.jpcamara.goson;

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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import gw.lang.reflect.IType;
import gw.lang.reflect.IPropertyInfo;
import gw.lang.reflect.TypeSystem;
import gw.lang.reflect.gs.IGosuObject;
import gw.lang.reflect.java.IJavaType;

public class Json implements IGosuObject {
  private JSONObject json;
  private IType type;
	
  public Json(IType type) {
    this.type = type;
    this.json = new JSONObject();
  }
	
  public Json(Object json, IType type) {
    if ((json instanceof JSONObject) == false) {
      throw new JSONParserException("Must be a JSONObject");
    }
    this.type = type;
    this.json = (JSONObject)json;
  }
	
	/**
	* Creates Json object, validating structure of the json 
	* string against the JsonTypeInfo provided.
	*/
	public Json(String json, JsonTypeInfo structure) {
		try {
		  this.type = structure.getOwnersType();
/*			this.json = new JSONObject(json);*/
			this.json = createJson(new JSONObject(json), structure);
		} catch (JSONException e) {
			throw new JSONParserException(e);
		}
	}
	
	//TODO add validity checks to verify the type is correct before parsing it.
  private JSONObject createJson(JSONObject it, JsonTypeInfo structure) throws JSONException {
    for (IPropertyInfo info : structure.getProperties()) {
      IType featureType = info.getFeatureType();
      String jsonName = structure.getJsonPropertyName(info.getName());
      if (!it.has(jsonName)) {
        continue;
      }
      Object o = it.get(jsonName);
      it.remove(jsonName);
      
      Parse parse = PARSE.get(featureType);
      if (parse != null) {
        it.put(jsonName, parse.parse(o));
      //Hackadoodledoo to determine if it's a custom json type
      } else if (!(featureType instanceof IJavaType) && !(featureType instanceof gw.internal.gosu.parser.IJavaTypeInternal)) {
        it.put(jsonName, new Json(createJson((JSONObject)o, (JsonTypeInfo)featureType.getTypeInfo()), featureType));
      } else if (featureType instanceof gw.internal.gosu.parser.IJavaTypeInternal) { //hack to determine list
        if (JsonParser.isJSONArray(o)) {
          JSONArray arr = (JSONArray)o;
          if (arr.length() == 0) {
            continue;
          }
          IType parameterizedType = featureType.getTypeParameters()[0];
          ArrayList rawList = createList(arr, parameterizedType);
          it.put(jsonName, (Object)rawList); //cast it so it doesn't get transformed into a jsonarray
        } else if (JsonParser.isJSONObject(o)) {
          JSONObject jsonObj = (JSONObject)o;
          Parse keyParser = PARSE.get(featureType.getTypeParameters()[0]);
          Parse valueParser = PARSE.get(featureType.getTypeParameters()[1]);
          if (keyParser == null || valueParser == null) {
            throw new RuntimeException("Map key and value currently only support simple types that conform to simple JSON");
          }
          
          Map jsonMap = new HashMap();
          Iterator iterate = jsonObj.keys();
          while (iterate.hasNext()) {
            String itKey = (String)iterate.next();
            jsonMap.put(keyParser.parse(itKey), valueParser.parse(jsonObj.get(itKey)));
          }
          it.put(jsonName, (Object)jsonMap);
        }
      }
    }
		return it;
	}
	
	private ArrayList createList(JSONArray arr, IType type) throws JSONException {
	  ArrayList list = new ArrayList();
	  for (int i = 0; i < arr.length(); i++) {
	    Parse parser = PARSE.get(type);
	    if (parser == null) {
	      list.add(new Json(createJson((JSONObject)arr.get(i), 
          (JsonTypeInfo)type.getTypeInfo()), type));
        continue;
	    }
	    list.add(parser.parse(arr.get(i)));
	  }
	  return list;
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
	
	public Json(Object json) {
		if ((json instanceof JSONObject) == false) {
			throw new JSONParserException("Must be a JSONObject");
		}
		this.json = (JSONObject)json;
	}
	
	public String serialize() {
		JSONObject output = serializeAsJSONObject();
		try {
      return output.toString(2);
		} catch (JSONException e) {
		  throw new RuntimeException(e);
		}
	}
	
	private JSONObject serializeAsJSONObject() {
		JSONObject output = new JSONObject();

		try {
			for (String key : keys()) {
				JsonName name = new JsonName(key);
				Object value = get(key);
				
				if (value instanceof List) {
				  
					List list = (List)value;
					JSONArray array = new JSONArray();
					if (output.has(name.getJsonName())) {
					  array = output.getJSONArray(name.getJsonName());
					} else {
					  output.put(name.getJsonName(), array);
					}
					
					if (list.size() > 0) {
/*            System.out.println("list of: " + list.get(0).getClass() + " - length: " + list.size());*/
					}
					if (list.size() > 0 && list.get(0) instanceof Json) {
						List<Json> jsons = (List<Json>)list;
						for (Json j : jsons) {
							array.put(j.serializeAsJSONObject());
						}
					} else {
						for (Object o : list) {
						  if (o instanceof Boolean || o instanceof String || o instanceof java.lang.Integer || o instanceof java.lang.Double) {
						    array.put(o);
						  } else if (o instanceof java.math.BigDecimal) {
						    array.put(((BigDecimal)o).doubleValue()); //TODO loss of precision
						  } else if (o instanceof java.math.BigInteger) {
						    array.put(((BigInteger)o).longValue());
						  } else if (o instanceof java.util.Date) {
						    array.put(o.toString());
						  }
						}
					}
					
				} else if (value instanceof Json) {
				  
				  
					Json current = (Json)value;//get(name.getJsonName());
					output.put(name.getJsonName(), current.serializeAsJSONObject());
					
					
				} else if (value instanceof JSONArray) {
				  
				  
				  JSONArray arr = (JSONArray)value;
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
				  }
				  
				  
				} else if (value instanceof java.util.Map) {
          handleJavaMapType(output, name.getJsonName(), (Map)value);
        } else {
          handleJavaSimpleType(output, name.getJsonName(), value);
        } 
			}
		} catch (JSONException e) {
			throw new RuntimeException(e);
		}
		
/*    System.out.println("End of writing: " + output.toString());*/
		return output;
	}
	
	private void handleJavaMapType(JSONObject output, String key, Map value) throws JSONException {
	  JSONObject j = new JSONObject();
    for (Object mapKey : ((Map)value).keySet()) {
      handleJavaSimpleType(j, mapKey.toString(), ((Map)value).get(mapKey));
    }
    output.put(key, j);
	}
	
	private void handleJavaSimpleType(JSONObject output, String key, Object value) throws JSONException {
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
		try {
			if (json.has(key) == false) {
				return null;
			}
			return json.get(key);
		} catch (JSONException e) {
			throw new JSONParserException(e);		
		}
	}
	
	public Object getWithIndex(String key, int index) {
		try {
			if (json.has(key) == false) {
				return null;
			}
			Object o = json.get(key);
			if (JsonParser.isJSONArray(o) == false) {
				throw new JSONParserException(key + " is not an array");
			}
			return ((JSONArray)o).get(index);
		} catch (JSONException e) {
			throw new JSONParserException(e);
		}
	}
	
	public void put(String key, Object value) {
		try {
			json.put(key, value);
		} catch (JSONException e) {
			throw new JSONParserException(e);
		}
	}
	
	public Json getJson(String key) {
		try {
			return new Json(json.getJSONObject(key), type);
		} catch (JSONException e) {
			throw new JSONParserException(e);
		}
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
		return new IterableJson();
	}
	
	@Override
	public IType getIntrinsicType() {
    return type;
	}
	
	private class IterableJson implements Iterable<String> {
		@SuppressWarnings("unchecked")
		@Override
		public Iterator<String> iterator() {
			return (Iterator<String>)json.keys();
		}
	}
}
