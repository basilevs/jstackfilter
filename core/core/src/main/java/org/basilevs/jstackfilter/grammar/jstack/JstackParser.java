/* JstackParser.java */
/* Generated By:JavaCC: Do not edit this line. JstackParser.java */
package org.basilevs.jstackfilter.grammar.jstack;
import org.basilevs.jstackfilter.Frame;
import org.basilevs.jstackfilter.KeyValue;
import org.basilevs.jstackfilter.JavaThread;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;
@ SuppressWarnings(
{
  "unused", "serial"
}

)
public final class JstackParser implements JstackParserConstants {
  public static void main(String [] args) throws ParseException, TokenMgrError
  {
    JstackParser parser = new JstackParser(System.in);
    parser.jstackDump();
  }

  final private void jstackHeader() throws ParseException {
    jj_consume_token(TIME);
    jj_consume_token(EOL);
    jj_consume_token(8);
    line();
    jj_consume_token(9);
    jj_consume_token(EOL);
    jj_consume_token(EOL);
    jj_consume_token(10);
    jj_consume_token(EOL);
    jj_consume_token(11);
    jj_consume_token(WORD);
    jj_consume_token(12);
    jj_consume_token(WORD);
    jj_consume_token(13);
    jj_consume_token(EOL);
    label_1:
    while (true) {
      switch ((jj_ntk==-1)?jj_ntk_f():jj_ntk) {
      case WORD:
      case NUMBER:
      case EOL:
      case SPACE:
      case 15:
      case 16:
      case 17:
      case 18:
      case 19:
      case 20:
      case 21:
      case 22:
      case 23:
      case 24:
      case 25:{
        ;
        break;
        }
      default:
        jj_la1[0] = jj_gen;
        break label_1;
      }
      line();
      jj_consume_token(EOL);
    }
    jj_consume_token(14);
    jj_consume_token(EOL);
}

  final private void line() throws ParseException {
    label_2:
    while (true) {
      switch ((jj_ntk==-1)?jj_ntk_f():jj_ntk) {
      case WORD:
      case NUMBER:
      case SPACE:
      case 15:
      case 16:
      case 17:
      case 18:
      case 19:
      case 20:
      case 21:
      case 22:
      case 23:
      case 24:
      case 25:{
        ;
        break;
        }
      default:
        jj_la1[1] = jj_gen;
        break label_2;
      }
      if (jj_2_1(2)) {
        jj_consume_token(15);
      } else {
        switch ((jj_ntk==-1)?jj_ntk_f():jj_ntk) {
        case 16:{
          jj_consume_token(16);
          break;
          }
        case 17:{
          jj_consume_token(17);
          break;
          }
        case 18:{
          jj_consume_token(18);
          break;
          }
        case 19:{
          jj_consume_token(19);
          break;
          }
        case 20:{
          jj_consume_token(20);
          break;
          }
        case 21:{
          jj_consume_token(21);
          break;
          }
        case 22:{
          jj_consume_token(22);
          break;
          }
        case 23:{
          jj_consume_token(23);
          break;
          }
        case 24:{
          jj_consume_token(24);
          break;
          }
        case 25:{
          jj_consume_token(25);
          break;
          }
        case WORD:{
          jj_consume_token(WORD);
          break;
          }
        case NUMBER:{
          jj_consume_token(NUMBER);
          break;
          }
        case SPACE:{
          jj_consume_token(SPACE);
          break;
          }
        default:
          jj_la1[2] = jj_gen;
          jj_consume_token(-1);
          throw new ParseException();
        }
      }
    }
}

  final private String method() throws ParseException {StringBuilder image = new StringBuilder();
  Token t;
    t = jj_consume_token(WORD);
image.append(t.image);
    label_3:
    while (true) {
      switch ((jj_ntk==-1)?jj_ntk_f():jj_ntk) {
      case WORD:{
        t = jj_consume_token(WORD);
image.append(t.image);
        break;
        }
      case NUMBER:{
        t = jj_consume_token(NUMBER);
image.append(t.image);
        break;
        }
      case 19:{
        jj_consume_token(19);
image.append(".");
        break;
        }
      case 22:{
        jj_consume_token(22);
image.append("$");
        break;
        }
      case 26:{
        jj_consume_token(26);
image.append("/");
        break;
        }
      case 23:{
        jj_consume_token(23);
        t = jj_consume_token(WORD);
        jj_consume_token(24);
image.append("<" + t.image + ">");
        break;
        }
      default:
        jj_la1[3] = jj_gen;
        jj_consume_token(-1);
        throw new ParseException();
      }
      if (jj_2_2(2)) {
        ;
      } else {
        break label_3;
      }
    }
{if ("" != null) return image.toString();}
    throw new Error("Missing return statement in function");
}

  final private String dottedIdentifier() throws ParseException {StringBuilder image = new StringBuilder();
  String method;
  Token t;
    label_4:
    while (true) {
      switch ((jj_ntk==-1)?jj_ntk_f():jj_ntk) {
      case WORD:{
        t = jj_consume_token(WORD);
image.append(t.image);
        break;
        }
      case 19:{
        jj_consume_token(19);
image.append(".");
        break;
        }
      case 22:{
        jj_consume_token(22);
image.append("$");
        break;
        }
      default:
        jj_la1[4] = jj_gen;
        jj_consume_token(-1);
        throw new ParseException();
      }
      switch ((jj_ntk==-1)?jj_ntk_f():jj_ntk) {
      case WORD:
      case 19:
      case 22:{
        ;
        break;
        }
      default:
        jj_la1[5] = jj_gen;
        break label_4;
      }
    }
{if ("" != null) return image.toString();}
    throw new Error("Missing return statement in function");
}

  final private Frame frame() throws ParseException {StringBuilder image = new StringBuilder();
  String method, p;
  Token t;
    jj_consume_token(27);
    method = method();
    jj_consume_token(15);
    if (jj_2_3(4)) {
      dottedIdentifier();
      switch ((jj_ntk==-1)?jj_ntk_f():jj_ntk) {
      case 28:{
        jj_consume_token(28);
        label_5:
        while (true) {
          switch ((jj_ntk==-1)?jj_ntk_f():jj_ntk) {
          case 19:{
            jj_consume_token(19);
            break;
            }
          case NUMBER:{
            jj_consume_token(NUMBER);
            break;
            }
          default:
            jj_la1[6] = jj_gen;
            jj_consume_token(-1);
            throw new ParseException();
          }
          switch ((jj_ntk==-1)?jj_ntk_f():jj_ntk) {
          case NUMBER:
          case 19:{
            ;
            break;
            }
          default:
            jj_la1[7] = jj_gen;
            break label_5;
          }
        }
        break;
        }
      default:
        jj_la1[8] = jj_gen;
        ;
      }
      jj_consume_token(26);
    } else {
      ;
    }
    switch ((jj_ntk==-1)?jj_ntk_f():jj_ntk) {
    case 29:{
      t = jj_consume_token(29);
image.append(t.image);
      break;
      }
    case 30:{
      t = jj_consume_token(30);
image.append(t.image);
      break;
      }
    case WORD:
    case 19:
    case 22:{
      p = dottedIdentifier();
image.append(p);
      switch ((jj_ntk==-1)?jj_ntk_f():jj_ntk) {
      case 9:{
        jj_consume_token(9);
        t = jj_consume_token(NUMBER);
image.append(":").append(t.image);
        break;
        }
      default:
        jj_la1[9] = jj_gen;
        ;
      }
      break;
      }
    default:
      jj_la1[10] = jj_gen;
      jj_consume_token(-1);
      throw new ParseException();
    }
    jj_consume_token(16);
{if ("" != null) return new Frame(method, image.toString());}
    throw new Error("Missing return statement in function");
}

  final public void monitor() throws ParseException {String line = "";
    switch ((jj_ntk==-1)?jj_ntk_f():jj_ntk) {
    case 31:{
      jj_consume_token(31);
      break;
      }
    case 32:{
      jj_consume_token(32);
      break;
      }
    case 33:{
      jj_consume_token(33);
      break;
      }
    case 34:{
      jj_consume_token(34);
      break;
      }
    default:
      jj_la1[11] = jj_gen;
      jj_consume_token(-1);
      throw new ParseException();
    }
    line();
}

  final public KeyValue keyValue() throws ParseException {Token key;
    Token t;
        StringBuilder value = new StringBuilder();
    key = jj_consume_token(WORD);
    jj_consume_token(35);
    label_6:
    while (true) {
      t = jj_consume_token(NUMBER);
value.append(t.image);
      switch ((jj_ntk==-1)?jj_ntk_f():jj_ntk) {
      case WORD:{
        t = jj_consume_token(WORD);
value.append(t.image);
        break;
        }
      default:
        jj_la1[12] = jj_gen;
        ;
      }
      switch ((jj_ntk==-1)?jj_ntk_f():jj_ntk) {
      case NUMBER:{
        ;
        break;
        }
      default:
        jj_la1[13] = jj_gen;
        break label_6;
      }
    }
{if ("" != null) return new KeyValue(key.image, value.toString());}
    throw new Error("Missing return statement in function");
}

//"VM Thread" os_prio=2 cpu=203.12ms elapsed=98699.54s tid=0x000002c22c0b2de0 nid=0x2c90 runnable
  final public void nativeThread() throws ParseException {
    jj_consume_token(QUOTE);
    label_7:
    while (true) {
      jj_consume_token(SPACE);
      if (jj_2_4(2)) {
        keyValue();
      } else {
        switch ((jj_ntk==-1)?jj_ntk_f():jj_ntk) {
        case WORD:{
          jj_consume_token(WORD);
          break;
          }
        default:
          jj_la1[14] = jj_gen;
          jj_consume_token(-1);
          throw new ParseException();
        }
      }
      if (jj_2_5(2)) {
        ;
      } else {
        break label_7;
      }
    }
    jj_consume_token(SPACE);
}

//"Worker-136" #2027 prio=5 os_prio=0 cpu=0.00ms elapsed=86.18s tid=0x000002c23074b590 nid=0x5884 in Object.wait()  [0x000000d6fb8fe000]
//   java.lang.Thread.State: TIMED_WAITING (on object monitor)
//	at java.lang.Object.wait(java.base@17/Native Method)
//	- waiting on <no object reference available>
//	at org.eclipse.core.internal.jobs.WorkerPool.sleep(WorkerPool.java:200)
//	- locked <0x0000000083286520> (a org.eclipse.core.internal.jobs.WorkerPool)
//	at org.eclipse.core.internal.jobs.WorkerPool.startJob(WorkerPool.java:242)
//	at org.eclipse.core.internal.jobs.Worker.run(Worker.java:58)
// or
//"main" #1 [755] prio=5 os_prio=0 cpu=2680,94ms elapsed=46236,64s tid=0x00007fb8d00285a0 nid=755 in Object.wait()  [0x00007fb8d7f56000]
//   java.lang.Thread.State: TIMED_WAITING (on object monitor)
//	at java.lang.Object.wait0(java.base@21.0.7/Native Method)
  final public JavaThread javaThread() throws ParseException {ArrayList < Frame > frames = new ArrayList < Frame > ();
  Frame f;
  Token t;
  String name;
  String state;
  long id = 0;
  KeyValue pair;
    t = jj_consume_token(QUOTE);
int length = t.image.length();
    name = t.image.substring(1, length - 1);
    jj_consume_token(SPACE);
    jj_consume_token(36);
    t = jj_consume_token(NUMBER);
id = Long.valueOf(t.image);
    jj_consume_token(SPACE);
    switch ((jj_ntk==-1)?jj_ntk_f():jj_ntk) {
    case 17:{
      jj_consume_token(17);
      jj_consume_token(NUMBER);
      jj_consume_token(18);
      jj_consume_token(SPACE);
      break;
      }
    default:
      jj_la1[15] = jj_gen;
      ;
    }
    label_8:
    while (true) {
      if (jj_2_6(2)) {
        pair = keyValue();
      } else if (jj_2_7(2)) {
        jj_consume_token(WORD);
      } else if (jj_2_8(2)) {
        jj_consume_token(37);
      } else {
        switch ((jj_ntk==-1)?jj_ntk_f():jj_ntk) {
        case 38:{
          jj_consume_token(38);
          break;
          }
        default:
          jj_la1[16] = jj_gen;
          jj_consume_token(-1);
          throw new ParseException();
        }
      }
      jj_consume_token(SPACE);
      if (jj_2_9(2)) {
        ;
      } else {
        break label_8;
      }
    }
    jj_consume_token(17);
    jj_consume_token(NUMBER);
    jj_consume_token(18);
    switch ((jj_ntk==-1)?jj_ntk_f():jj_ntk) {
    case EOL:{
      jj_consume_token(EOL);
      break;
      }
    default:
      jj_la1[17] = jj_gen;
      ;
    }
    jj_consume_token(SPACE);
    switch ((jj_ntk==-1)?jj_ntk_f():jj_ntk) {
    case 39:{
      jj_consume_token(39);
      t = jj_consume_token(WORD);
state = t.image;
      switch ((jj_ntk==-1)?jj_ntk_f():jj_ntk) {
      case SPACE:{
        jj_consume_token(SPACE);
        jj_consume_token(15);
state += " (";
        label_9:
        while (true) {
          switch ((jj_ntk==-1)?jj_ntk_f():jj_ntk) {
          case WORD:
          case SPACE:{
            ;
            break;
            }
          default:
            jj_la1[18] = jj_gen;
            break label_9;
          }
          switch ((jj_ntk==-1)?jj_ntk_f():jj_ntk) {
          case WORD:{
            t = jj_consume_token(WORD);
state += t.image;
            break;
            }
          case SPACE:{
            t = jj_consume_token(SPACE);
state += t.image;
            break;
            }
          default:
            jj_la1[19] = jj_gen;
            jj_consume_token(-1);
            throw new ParseException();
          }
        }
        jj_consume_token(16);
state += ")";
        break;
        }
      default:
        jj_la1[20] = jj_gen;
        ;
      }
      break;
      }
    case 40:{
      jj_consume_token(40);
      t = jj_consume_token(NUMBER);
state = "Carrying virtual thread #" + t.image;
      break;
      }
    default:
      jj_la1[21] = jj_gen;
      jj_consume_token(-1);
      throw new ParseException();
    }
    jj_consume_token(EOL);
    switch ((jj_ntk==-1)?jj_ntk_f():jj_ntk) {
    case SPACE:{
      jj_consume_token(SPACE);
      switch ((jj_ntk==-1)?jj_ntk_f():jj_ntk) {
      case 41:{
        jj_consume_token(41);
        jj_consume_token(EOL);
        break;
        }
      case 42:{
        jj_consume_token(42);
        line();
        break;
        }
      default:
        jj_la1[22] = jj_gen;
        jj_consume_token(-1);
        throw new ParseException();
      }
      break;
      }
    default:
      jj_la1[25] = jj_gen;
      label_10:
      while (true) {
        switch ((jj_ntk==-1)?jj_ntk_f():jj_ntk) {
        case FRAMEINDENT:{
          ;
          break;
          }
        default:
          jj_la1[23] = jj_gen;
          break label_10;
        }
        jj_consume_token(FRAMEINDENT);
        switch ((jj_ntk==-1)?jj_ntk_f():jj_ntk) {
        case 31:
        case 32:
        case 33:
        case 34:{
          monitor();
          break;
          }
        case 27:{
          f = frame();
frames.add(f);
          break;
          }
        default:
          jj_la1[24] = jj_gen;
          jj_consume_token(-1);
          throw new ParseException();
        }
        jj_consume_token(EOL);
      }
    }
{if ("" != null) return new JavaThread(name, id, state, frames);}
    throw new Error("Missing return statement in function");
}

  final public Collection < JavaThread > threads() throws ParseException {ArrayList < JavaThread > javaThreads = new ArrayList < JavaThread > ();
  JavaThread thread;
    label_11:
    while (true) {
      switch ((jj_ntk==-1)?jj_ntk_f():jj_ntk) {
      case QUOTE:{
        ;
        break;
        }
      default:
        jj_la1[26] = jj_gen;
        break label_11;
      }
      if (jj_2_10(3)) {
        thread = javaThread();
javaThreads.add(thread);
      } else {
        switch ((jj_ntk==-1)?jj_ntk_f():jj_ntk) {
        case QUOTE:{
          nativeThread();
          break;
          }
        default:
          jj_la1[27] = jj_gen;
          jj_consume_token(-1);
          throw new ParseException();
        }
      }
      jj_consume_token(EOL);
    }
{if ("" != null) return javaThreads;}
    throw new Error("Missing return statement in function");
}

  final public Collection < JavaThread > jstackDump() throws ParseException {Collection < JavaThread > javaThreads;
    jstackHeader();
    jj_consume_token(EOL);
    javaThreads = threads();
{if ("" != null) return javaThreads;}
    throw new Error("Missing return statement in function");
}

  private boolean jj_2_1(int xla)
 {
    jj_la = xla; jj_lastpos = jj_scanpos = token;
    try { return (!jj_3_1()); }
    catch(LookaheadSuccess ls) { return true; }
    finally { jj_save(0, xla); }
  }

  private boolean jj_2_2(int xla)
 {
    jj_la = xla; jj_lastpos = jj_scanpos = token;
    try { return (!jj_3_2()); }
    catch(LookaheadSuccess ls) { return true; }
    finally { jj_save(1, xla); }
  }

  private boolean jj_2_3(int xla)
 {
    jj_la = xla; jj_lastpos = jj_scanpos = token;
    try { return (!jj_3_3()); }
    catch(LookaheadSuccess ls) { return true; }
    finally { jj_save(2, xla); }
  }

  private boolean jj_2_4(int xla)
 {
    jj_la = xla; jj_lastpos = jj_scanpos = token;
    try { return (!jj_3_4()); }
    catch(LookaheadSuccess ls) { return true; }
    finally { jj_save(3, xla); }
  }

  private boolean jj_2_5(int xla)
 {
    jj_la = xla; jj_lastpos = jj_scanpos = token;
    try { return (!jj_3_5()); }
    catch(LookaheadSuccess ls) { return true; }
    finally { jj_save(4, xla); }
  }

  private boolean jj_2_6(int xla)
 {
    jj_la = xla; jj_lastpos = jj_scanpos = token;
    try { return (!jj_3_6()); }
    catch(LookaheadSuccess ls) { return true; }
    finally { jj_save(5, xla); }
  }

  private boolean jj_2_7(int xla)
 {
    jj_la = xla; jj_lastpos = jj_scanpos = token;
    try { return (!jj_3_7()); }
    catch(LookaheadSuccess ls) { return true; }
    finally { jj_save(6, xla); }
  }

  private boolean jj_2_8(int xla)
 {
    jj_la = xla; jj_lastpos = jj_scanpos = token;
    try { return (!jj_3_8()); }
    catch(LookaheadSuccess ls) { return true; }
    finally { jj_save(7, xla); }
  }

  private boolean jj_2_9(int xla)
 {
    jj_la = xla; jj_lastpos = jj_scanpos = token;
    try { return (!jj_3_9()); }
    catch(LookaheadSuccess ls) { return true; }
    finally { jj_save(8, xla); }
  }

  private boolean jj_2_10(int xla)
 {
    jj_la = xla; jj_lastpos = jj_scanpos = token;
    try { return (!jj_3_10()); }
    catch(LookaheadSuccess ls) { return true; }
    finally { jj_save(9, xla); }
  }

  private boolean jj_3_8()
 {
    if (jj_scan_token(37)) return true;
    return false;
  }

  private boolean jj_3R_frame_224_7_19()
 {
    if (jj_scan_token(28)) return true;
    Token xsp;
    if (jj_3R_frame_226_9_23()) return true;
    while (true) {
      xsp = jj_scanpos;
      if (jj_3R_frame_226_9_23()) { jj_scanpos = xsp; break; }
    }
    return false;
  }

  private boolean jj_3_7()
 {
    if (jj_scan_token(WORD)) return true;
    return false;
  }

  private boolean jj_3_6()
 {
    if (jj_3R_keyValue_282_3_20()) return true;
    return false;
  }

  private boolean jj_3_3()
 {
    if (jj_3R_dottedIdentifier_187_3_18()) return true;
    Token xsp;
    xsp = jj_scanpos;
    if (jj_3R_frame_224_7_19()) jj_scanpos = xsp;
    if (jj_scan_token(26)) return true;
    return false;
  }

  private boolean jj_3_4()
 {
    if (jj_3R_keyValue_282_3_20()) return true;
    return false;
  }

  private boolean jj_3_9()
 {
    Token xsp;
    xsp = jj_scanpos;
    if (jj_3_6()) {
    jj_scanpos = xsp;
    if (jj_3_7()) {
    jj_scanpos = xsp;
    if (jj_3_8()) {
    jj_scanpos = xsp;
    if (jj_scan_token(38)) return true;
    }
    }
    }
    if (jj_scan_token(SPACE)) return true;
    return false;
  }

  private boolean jj_3R_method_167_7_17()
 {
    if (jj_scan_token(23)) return true;
    if (jj_scan_token(WORD)) return true;
    return false;
  }

  private boolean jj_3_5()
 {
    if (jj_scan_token(SPACE)) return true;
    Token xsp;
    xsp = jj_scanpos;
    if (jj_3_4()) {
    jj_scanpos = xsp;
    if (jj_scan_token(2)) return true;
    }
    return false;
  }

  private boolean jj_3R_method_162_7_16()
 {
    if (jj_scan_token(26)) return true;
    return false;
  }

  private boolean jj_3_10()
 {
    if (jj_3R_javaThread_344_3_21()) return true;
    return false;
  }

  private boolean jj_3R_method_157_7_15()
 {
    if (jj_scan_token(22)) return true;
    return false;
  }

  private boolean jj_3R_javaThread_344_3_21()
 {
    if (jj_scan_token(QUOTE)) return true;
    if (jj_scan_token(SPACE)) return true;
    if (jj_scan_token(36)) return true;
    return false;
  }

  private boolean jj_3R_method_152_7_14()
 {
    if (jj_scan_token(19)) return true;
    return false;
  }

  private boolean jj_3R_dottedIdentifier_198_5_26()
 {
    if (jj_scan_token(22)) return true;
    return false;
  }

  private boolean jj_3R_method_147_7_13()
 {
    if (jj_scan_token(NUMBER)) return true;
    return false;
  }

  private boolean jj_3R_dottedIdentifier_193_5_25()
 {
    if (jj_scan_token(19)) return true;
    return false;
  }

  private boolean jj_3_1()
 {
    if (jj_scan_token(15)) return true;
    return false;
  }

  private boolean jj_3R_method_142_7_12()
 {
    if (jj_scan_token(WORD)) return true;
    return false;
  }

  private boolean jj_3R_dottedIdentifier_188_5_24()
 {
    if (jj_scan_token(WORD)) return true;
    return false;
  }

  private boolean jj_3R_dottedIdentifier_188_5_22()
 {
    Token xsp;
    xsp = jj_scanpos;
    if (jj_3R_dottedIdentifier_188_5_24()) {
    jj_scanpos = xsp;
    if (jj_3R_dottedIdentifier_193_5_25()) {
    jj_scanpos = xsp;
    if (jj_3R_dottedIdentifier_198_5_26()) return true;
    }
    }
    return false;
  }

  private boolean jj_3_2()
 {
    Token xsp;
    xsp = jj_scanpos;
    if (jj_3R_method_142_7_12()) {
    jj_scanpos = xsp;
    if (jj_3R_method_147_7_13()) {
    jj_scanpos = xsp;
    if (jj_3R_method_152_7_14()) {
    jj_scanpos = xsp;
    if (jj_3R_method_157_7_15()) {
    jj_scanpos = xsp;
    if (jj_3R_method_162_7_16()) {
    jj_scanpos = xsp;
    if (jj_3R_method_167_7_17()) return true;
    }
    }
    }
    }
    }
    return false;
  }

  private boolean jj_3R_keyValue_282_3_20()
 {
    if (jj_scan_token(WORD)) return true;
    if (jj_scan_token(35)) return true;
    return false;
  }

  private boolean jj_3R_dottedIdentifier_187_3_18()
 {
    Token xsp;
    if (jj_3R_dottedIdentifier_188_5_22()) return true;
    while (true) {
      xsp = jj_scanpos;
      if (jj_3R_dottedIdentifier_188_5_22()) { jj_scanpos = xsp; break; }
    }
    return false;
  }

  private boolean jj_3R_frame_226_9_23()
 {
    Token xsp;
    xsp = jj_scanpos;
    if (jj_scan_token(19)) {
    jj_scanpos = xsp;
    if (jj_scan_token(3)) return true;
    }
    return false;
  }

  /** Generated Token Manager. */
  public JstackParserTokenManager token_source;
  SimpleCharStream jj_input_stream;
  /** Current token. */
  public Token token;
  /** Next token. */
  public Token jj_nt;
  private int jj_ntk;
  private Token jj_scanpos, jj_lastpos;
  private int jj_la;
  private int jj_gen;
  final private int[] jj_la1 = new int[28];
  static private int[] jj_la1_0;
  static private int[] jj_la1_1;
  static {
	   jj_la1_init_0();
	   jj_la1_init_1();
	}
	private static void jj_la1_init_0() {
	   jj_la1_0 = new int[] {0x3ff805c,0x3ff804c,0x3ff004c,0x4c8000c,0x480004,0x480004,0x80008,0x80008,0x10000000,0x200,0x60480004,0x80000000,0x4,0x8,0x4,0x20000,0x0,0x10,0x44,0x44,0x40,0x0,0x0,0x20,0x88000000,0x40,0x2,0x2,};
	}
	private static void jj_la1_init_1() {
	   jj_la1_1 = new int[] {0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x0,0x7,0x0,0x0,0x0,0x0,0x40,0x0,0x0,0x0,0x0,0x180,0x600,0x0,0x7,0x0,0x0,0x0,};
	}
  final private JJCalls[] jj_2_rtns = new JJCalls[10];
  private boolean jj_rescan = false;
  private int jj_gc = 0;

  /** Constructor with InputStream. */
  public JstackParser(java.io.InputStream stream) {
	  this(stream, null);
  }
  /** Constructor with InputStream and supplied encoding */
  public JstackParser(java.io.InputStream stream, String encoding) {
	 try { jj_input_stream = new SimpleCharStream(stream, encoding, 1, 1); } catch(java.io.UnsupportedEncodingException e) { throw new RuntimeException(e); }
	 token_source = new JstackParserTokenManager(jj_input_stream);
	 token = new Token();
	 jj_ntk = -1;
	 jj_gen = 0;
	 for (int i = 0; i < 28; i++) jj_la1[i] = -1;
	 for (int i = 0; i < jj_2_rtns.length; i++) jj_2_rtns[i] = new JJCalls();
  }

  /** Reinitialise. */
  public void ReInit(java.io.InputStream stream) {
	  ReInit(stream, null);
  }
  /** Reinitialise. */
  public void ReInit(java.io.InputStream stream, String encoding) {
	 try { jj_input_stream.ReInit(stream, encoding, 1, 1); } catch(java.io.UnsupportedEncodingException e) { throw new RuntimeException(e); }
	 token_source.ReInit(jj_input_stream);
	 token = new Token();
	 jj_ntk = -1;
	 jj_gen = 0;
	 for (int i = 0; i < 28; i++) jj_la1[i] = -1;
	 for (int i = 0; i < jj_2_rtns.length; i++) jj_2_rtns[i] = new JJCalls();
  }

  /** Constructor. */
  public JstackParser(java.io.Reader stream) {
	 jj_input_stream = new SimpleCharStream(stream, 1, 1);
	 token_source = new JstackParserTokenManager(jj_input_stream);
	 token = new Token();
	 jj_ntk = -1;
	 jj_gen = 0;
	 for (int i = 0; i < 28; i++) jj_la1[i] = -1;
	 for (int i = 0; i < jj_2_rtns.length; i++) jj_2_rtns[i] = new JJCalls();
  }

  /** Reinitialise. */
  public void ReInit(java.io.Reader stream) {
	if (jj_input_stream == null) {
	   jj_input_stream = new SimpleCharStream(stream, 1, 1);
	} else {
	   jj_input_stream.ReInit(stream, 1, 1);
	}
	if (token_source == null) {
 token_source = new JstackParserTokenManager(jj_input_stream);
	}

	 token_source.ReInit(jj_input_stream);
	 token = new Token();
	 jj_ntk = -1;
	 jj_gen = 0;
	 for (int i = 0; i < 28; i++) jj_la1[i] = -1;
	 for (int i = 0; i < jj_2_rtns.length; i++) jj_2_rtns[i] = new JJCalls();
  }

  /** Constructor with generated Token Manager. */
  public JstackParser(JstackParserTokenManager tm) {
	 token_source = tm;
	 token = new Token();
	 jj_ntk = -1;
	 jj_gen = 0;
	 for (int i = 0; i < 28; i++) jj_la1[i] = -1;
	 for (int i = 0; i < jj_2_rtns.length; i++) jj_2_rtns[i] = new JJCalls();
  }

  /** Reinitialise. */
  public void ReInit(JstackParserTokenManager tm) {
	 token_source = tm;
	 token = new Token();
	 jj_ntk = -1;
	 jj_gen = 0;
	 for (int i = 0; i < 28; i++) jj_la1[i] = -1;
	 for (int i = 0; i < jj_2_rtns.length; i++) jj_2_rtns[i] = new JJCalls();
  }

  private Token jj_consume_token(int kind) throws ParseException {
	 Token oldToken;
	 if ((oldToken = token).next != null) token = token.next;
	 else token = token.next = token_source.getNextToken();
	 jj_ntk = -1;
	 if (token.kind == kind) {
	   jj_gen++;
	   if (++jj_gc > 100) {
		 jj_gc = 0;
		 for (int i = 0; i < jj_2_rtns.length; i++) {
		   JJCalls c = jj_2_rtns[i];
		   while (c != null) {
			 if (c.gen < jj_gen) c.first = null;
			 c = c.next;
		   }
		 }
	   }
	   return token;
	 }
	 token = oldToken;
	 jj_kind = kind;
	 throw generateParseException();
  }

  @SuppressWarnings("serial")
  static private final class LookaheadSuccess extends java.lang.Error {
    @Override
    public Throwable fillInStackTrace() {
      return this;
    }
  }
  static private final LookaheadSuccess jj_ls = new LookaheadSuccess();
  private boolean jj_scan_token(int kind) {
	 if (jj_scanpos == jj_lastpos) {
	   jj_la--;
	   if (jj_scanpos.next == null) {
		 jj_lastpos = jj_scanpos = jj_scanpos.next = token_source.getNextToken();
	   } else {
		 jj_lastpos = jj_scanpos = jj_scanpos.next;
	   }
	 } else {
	   jj_scanpos = jj_scanpos.next;
	 }
	 if (jj_rescan) {
	   int i = 0; Token tok = token;
	   while (tok != null && tok != jj_scanpos) { i++; tok = tok.next; }
	   if (tok != null) jj_add_error_token(kind, i);
	 }
	 if (jj_scanpos.kind != kind) return true;
	 if (jj_la == 0 && jj_scanpos == jj_lastpos) throw jj_ls;
	 return false;
  }


/** Get the next Token. */
  final public Token getNextToken() {
	 if (token.next != null) token = token.next;
	 else token = token.next = token_source.getNextToken();
	 jj_ntk = -1;
	 jj_gen++;
	 return token;
  }

/** Get the specific Token. */
  final public Token getToken(int index) {
	 Token t = token;
	 for (int i = 0; i < index; i++) {
	   if (t.next != null) t = t.next;
	   else t = t.next = token_source.getNextToken();
	 }
	 return t;
  }

  private int jj_ntk_f() {
	 if ((jj_nt=token.next) == null)
	   return (jj_ntk = (token.next=token_source.getNextToken()).kind);
	 else
	   return (jj_ntk = jj_nt.kind);
  }

  private java.util.List<int[]> jj_expentries = new java.util.ArrayList<int[]>();
  private int[] jj_expentry;
  private int jj_kind = -1;
  private int[] jj_lasttokens = new int[100];
  private int jj_endpos;

  private void jj_add_error_token(int kind, int pos) {
	 if (pos >= 100) {
		return;
	 }

	 if (pos == jj_endpos + 1) {
	   jj_lasttokens[jj_endpos++] = kind;
	 } else if (jj_endpos != 0) {
	   jj_expentry = new int[jj_endpos];

	   for (int i = 0; i < jj_endpos; i++) {
		 jj_expentry[i] = jj_lasttokens[i];
	   }

	   for (int[] oldentry : jj_expentries) {
		 if (oldentry.length == jj_expentry.length) {
		   boolean isMatched = true;

		   for (int i = 0; i < jj_expentry.length; i++) {
			 if (oldentry[i] != jj_expentry[i]) {
			   isMatched = false;
			   break;
			 }

		   }
		   if (isMatched) {
			 jj_expentries.add(jj_expentry);
			 break;
		   }
		 }
	   }

	   if (pos != 0) {
		 jj_lasttokens[(jj_endpos = pos) - 1] = kind;
	   }
	 }
  }

  /** Generate ParseException. */
  public ParseException generateParseException() {
	 jj_expentries.clear();
	 boolean[] la1tokens = new boolean[43];
	 if (jj_kind >= 0) {
	   la1tokens[jj_kind] = true;
	   jj_kind = -1;
	 }
	 for (int i = 0; i < 28; i++) {
	   if (jj_la1[i] == jj_gen) {
		 for (int j = 0; j < 32; j++) {
		   if ((jj_la1_0[i] & (1<<j)) != 0) {
			 la1tokens[j] = true;
		   }
		   if ((jj_la1_1[i] & (1<<j)) != 0) {
			 la1tokens[32+j] = true;
		   }
		 }
	   }
	 }
	 for (int i = 0; i < 43; i++) {
	   if (la1tokens[i]) {
		 jj_expentry = new int[1];
		 jj_expentry[0] = i;
		 jj_expentries.add(jj_expentry);
	   }
	 }
	 jj_endpos = 0;
	 jj_rescan_token();
	 jj_add_error_token(0, 0);
	 int[][] exptokseq = new int[jj_expentries.size()][];
	 for (int i = 0; i < jj_expentries.size(); i++) {
	   exptokseq[i] = jj_expentries.get(i);
	 }
	 return new ParseException(token, exptokseq, tokenImage);
  }

  private boolean trace_enabled;

/** Trace enabled. */
  final public boolean trace_enabled() {
	 return trace_enabled;
  }

  /** Enable tracing. */
  final public void enable_tracing() {
  }

  /** Disable tracing. */
  final public void disable_tracing() {
  }

  private void jj_rescan_token() {
	 jj_rescan = true;
	 for (int i = 0; i < 10; i++) {
	   try {
		 JJCalls p = jj_2_rtns[i];

		 do {
		   if (p.gen > jj_gen) {
			 jj_la = p.arg; jj_lastpos = jj_scanpos = p.first;
			 switch (i) {
			   case 0: jj_3_1(); break;
			   case 1: jj_3_2(); break;
			   case 2: jj_3_3(); break;
			   case 3: jj_3_4(); break;
			   case 4: jj_3_5(); break;
			   case 5: jj_3_6(); break;
			   case 6: jj_3_7(); break;
			   case 7: jj_3_8(); break;
			   case 8: jj_3_9(); break;
			   case 9: jj_3_10(); break;
			 }
		   }
		   p = p.next;
		 } while (p != null);

		 } catch(LookaheadSuccess ls) { }
	 }
	 jj_rescan = false;
  }

  private void jj_save(int index, int xla) {
	 JJCalls p = jj_2_rtns[index];
	 while (p.gen > jj_gen) {
	   if (p.next == null) { p = p.next = new JJCalls(); break; }
	   p = p.next;
	 }

	 p.gen = jj_gen + xla - jj_la; 
	 p.first = token;
	 p.arg = xla;
  }

  static final class JJCalls {
	 int gen;
	 Token first;
	 int arg;
	 JJCalls next;
  }

}
