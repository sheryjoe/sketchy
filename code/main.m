startup;
nTrials = 1;

params.nIm = 1;
params.svmCross = 0;
params.crossType = 1; % cross-val on acuracy

params.svmKern = 0; %0=dot, 1=intersect

params.sameClass = 1;
params.classTrain = [0];
params.classTest = [0]; % only used if sameClass == 0

params.poolMode = 0; %0=mean,1=max,2=sum
params.pooling = [1,1;2,2;4,4];

% 0 = gabors, do not split pos/neg features
% 1 = gabors, split pos/neg features
% 2 = 3x3 local descriptor, summarize locally
% 3 = 3x3 local descriptor, say if pixels have specific neighbours
params.featType = 0;

for (t=1:nTrials)
    display(['On trial ', int2str(t)]);
    [multiClass(t,1),confuse(:,:,t),~,tp(t,:),fp(t,:)] = mainPartClassify(params,t);
end
% 
% params.featType = 1;
% 
% for (t=1:nTrials)
%     display(['On trial ', int2str(t)]);
%     [multiClass(t,1),confuse(:,:,t),~,tp(t,:),fp(t,:)] = mainPartClassify(params,t);
% end

%close all; plot(fp,tp,'b-o'); hold on; plot([0:0.01:1],[0:0.01:1],'r');