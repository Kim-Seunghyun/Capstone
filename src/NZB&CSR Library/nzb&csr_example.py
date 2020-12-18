#© 2020 Gihyun Park <texnee@gmail.com>

import torch
import torch.nn as nn
import torch.nn.functional as F
import torch.optim as optim
from torchvision import datasets, transforms
from torch.autograd import Variable

import torch.nn.utils.prune as prune 
from matplotlib import pyplot as plt
import numpy as np

#커스텀 모듈 불러오기
from NZB import *
from CSR import *

#디바이스 설정
device = 'cuda' if torch.cuda.is_available()else 'cpu'
torch.manual_seed(777) 
if device == 'cuda': 
    torch.cuda.manual_seed_all(123)

#모델 선언 및 생성 후 pruning
class MnistModel(nn.Module):
    def __init__(self):
        super(MnistModel, self).__init__()
        # input is 28x28
        # padding=2 for same padding
        self.conv1 = nn.Conv2d(1, 32, 3, padding=1)#nn.Conv2d(1, 32, 3, padding=1)
        # feature map size is 14*14 by pooling
        # padding=2 for same padding
        self.conv2 = nn.Conv2d(32, 64, 3, padding=1)
        # feature map size is 7*7 by pooling
        self.fc1 = nn.Linear(64*7*7, 1024)
        self.fc2 = nn.Linear(1024, 10)

    def forward(self, x, nzb1 = 0, nzb2 = 0, infer = 0):
        if(infer == 1):
            nzbs1 = Nzbs(self.conv1.weight.cpu().detach().numpy()) #weight를 nzb로 변환
            nzbs2 = Nzbs(self.conv2.weight.cpu().detach().numpy())

            x = F.max_pool2d(F.relu(Myconv_nzb(x.cpu(), nzbs1, self.conv1.bias.cpu().detach().numpy()).to('cuda')), 2) #nzb conv1
            x = F.max_pool2d(F.relu(Myconv_nzb(x.cpu(), nzbs2, self.conv2.bias.cpu().detach().numpy()).to('cuda')), 2) #nzb conv2
        elif(infer == 2):
            csrs1 = csrs(self.conv1.weight.cpu().detach().numpy()) #weight를 nzb로 변환
            csrs2 = csrs(self.conv2.weight.cpu().detach().numpy())

            x = F.max_pool2d(F.relu(Myconv_csr(x.cpu(), csrs1, self.conv1.bias.cpu().detach().numpy()).to('cuda')), 2) #nzb conv1
            x = F.max_pool2d(F.relu(Myconv_csr(x.cpu(), csrs2, self.conv2.bias.cpu().detach().numpy()).to('cuda')), 2) #nzb conv2
        else:
            x = F.max_pool2d(F.relu(self.conv1(x)), 2) 
            x = F.max_pool2d(F.relu(self.conv2(x)), 2)

        x = x.view(-1, 64*7*7)   # reshape Variable
        x = F.relu(self.fc1(x))
        x = F.dropout(x, training=self.training)
        x = self.fc2(x)
        return F.log_softmax(x)
    
model = MnistModel().to(device)
prune.random_unstructured(model.conv1, name="weight", amount=0.7) #pruning
prune.random_unstructured(model.conv2, name="weight", amount=0.7)

#트레인 로더 설정
batch_size = 50
train_loader = torch.utils.data.DataLoader(
    datasets.MNIST('data', train=True, download=True, transform=transforms.ToTensor()),
    batch_size=batch_size, shuffle=True)

#테스트 로더 설정
test_loader = torch.utils.data.DataLoader(
    datasets.MNIST('data', train=False, transform=transforms.ToTensor()),
    batch_size=1)

optimizer = optim.Adam(model.parameters(), lr=0.0001)

#학습 시작 GPU로 연산한다.
model.train()
train_loss = []
train_accu = []
i = 0
for epoch in range(2): # 15
    for data, target in train_loader:
        data, target = data.to(device), target.to(device)
        optimizer.zero_grad()
        output = model.forward(data)
        loss = F.nll_loss(output, target)
        loss.backward()    # calc gradients
        train_loss.append(loss.data)
        optimizer.step()   # update gradients
        prediction = output.data.max(1)[1]   # first column has actual prob.
        accuracy = prediction.eq(target.data).sum()/batch_size*100
        train_accu.append(accuracy)
        if i % 1000 == 0:
            print('Train Step: {}\tLoss: {:.3f}\tAccuracy: {:.3f}'.format(i, loss.data, accuracy))
        i += 1

#테스트 시작 CPU로 연산한다.
model.eval()
correct = 0
for data, target in test_loader:
    start = time.time()
    data, target = data.to(device), target.to(device)
    output = model.forward(data, 0, 0 ,1) #0은 default 1은 nzb, 2는 nzb방식으로 테스트한다.
    prediction = output.data.max(1)[1]
    print(prediction, target)
    correct += prediction.eq(target.data).sum()
    print("correct: ", correct)
    nzb_n.append(time.time()-start)
    n += 1
    if(n == 10):
        break;
print('\nTest set: Accuracy: {:.2f}%'.format(100. * correct / len(test_loader.dataset)))

#nzb csr 크기 비교
import sys

weight = model.conv2.weight
weight_csr = csrs(weight)
weight_nzb = nzbs(weight)

print(sys.getsizeof(weight_csr.csrCols) + sys.getsizeof(weight_csr.csrRows))
print(sys.getsizeof(weight_nzb.nzbWeight))