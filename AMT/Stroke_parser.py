import re;

def outputStrokeSvg(strokes,outName):
    res = [];
    res.append('<?xml version=\"1.0\" encoding=\"utf-8\"?>');
    res.append('<!DOCTYPE svg PUBLIC "-//W3C//DTD SVG 1.1//EN" "http://www.w3.org/Graphics/SVG/1.1/DTD/svg11.dtd">');
    res.append('<svg viewBox="0 0 800 800" preserveAspectRatio="xMinYMin meet" xmlns="http://www.w3.org/2000/svg" version="1.1">');
    res.append('<g fill="none" stroke="#FF0000">');
    for i in strokes:
        res.append(i);
    res.append('</g>');
    res.append('</svg>');

    f = open(outName,'w');
    for i in res:
        f.write(i);
    f.close();

def mergeSvgs(svgSketch,strokes):

    # ratio of original canvas size to annotation size
    scaleFactor = float(800)/420;
    
    m = 'path id=\"(\d+)\"';
    res = svgSketch[:-3];
    res.append('</g>');
    res.append('</g>');
    res.append('<g fill="none" stroke="#FF0000">');
    
    maxId = -1;
    for line in svgSketch:
        a = re.search(m,line);
        if a:
            n = int(a.group(1));
            maxId = max(n,maxId);


    res.append('<g transform=\"scale(' + str(scaleFactor) + ')\">');
    # assume pathid starts from 0
    for i in range(len(strokes)):
        temp = re.sub('path id=\"' +
                      str(i) +
                      '\"','path id=\"'
                      + str(i+maxId+1) + '\"', strokes[i]);
        res.append(temp);
    res.append('</g>');
    res.append('</g>');
    res.append('</svg>');
    
    return res;

def parse_line(line):
    mColor = 'stroke=(.+?)';
    mStroke = 'path=\[(.+)\]';
    
    line = re.sub(':','=',line);
    line = re.sub('\"','',line);
    
    s = re.findall(mStroke,line);

    assert(len(s)==1);
    s=s[0];
        
    s = re.sub('M,','M',s);
    s = re.sub('C,','C',s);
    s = re.sub('[\[|\]]','',s);
    s = re.sub(',', ' ', s);

    return s;

    
# parses all strokes from one giant string
def parse_strokes(strokes):
    m='(\\{.+?\\})+';

    st=re.findall(m,strokes)
    
    res = [];
    for i in range(len(st)):
        res.append('<path id=\"' + str(i) + '\" d=\"' + parse_line(st[i]) + ' \"/>');
    return res;
