#© 2020 Gihyun Park <texnee@gmail.com>

import torch

'''
커널을 csr로 만들어주는 함수
인덱스(bytearray)와 데이터(list)를 반환한다.
'''
def convertCsr(kernel): # 3x3 single kernel
    data = [] #커널 실제 데이터
    row = []
    col = []
    rowData = 0
    for i in range(3):
        for j in range(3):
            if(kernel[i][j] != 0):
                rowData += 1
                col.append(j)
                #print(j)
                data.append(kernel[i][j])
        row.append(rowData)

    return row, col, data

'''
CSR전용 컨볼루션 함수
하나의 사진과 필터를 컨볼루션한다.
'''
def convolve2D_csr(image, row, col, data, padding=1, strides=1): #한 개의 data와 kernel을 convolve한다.
    kernShape = 3
    imgShape = image.shape[0]

    output = int(((imgShape - kernShape + 2 * padding) / strides) + 1)
    output = torch.zeros(output, output).to('cuda')

    if padding != 0:
        imagePadded = torch.zeros(imgShape + padding*2, imgShape + padding*2).to('cuda')
        imagePadded[int(padding):int(-1 * padding), int(padding):int(-1 * padding)] = image
    else:
        imagePadded = image
    
    for i in range(imgShape): #가로
        for j in range(imgShape): #세로
            n = 0 #data
            m = 0 #col
            dataNum = row[0]
            for k in range(dataNum):
                output[i][j] += imagePadded[i][j+col[m]]*data[n]
                n += 1
                m += 1
            dataNum = row[1]-row[0]
            for k in range(dataNum):
                output[i][j] += imagePadded[i+1][j+col[m]]*data[n]
                n += 1
                m += 1
            dataNum = row[2]-row[1]
            for k in range(dataNum):
                output[i][j] += imagePadded[i+2][j+col[m]]*data[n]
                n += 1
                m += 1
                      
    return output

'''
기존의 커널들이 있는 텐서들을 CSR 텐서로 만들어주는 Class
'''
class csrs:
    def __init__(self, weight):
        self.first = weight.shape[0]
        self.second = weight.shape[1]
        self.csrRows = []
        self.csrCols = []
        self.csrData = []
        for i in range(self.first):
            for j in range(self.second):
                row, col, data = convertCsr(weight[i][j])
                self.csrRows.append(row)
                self.csrCols.append(col)
                self.csrData.append(data)

